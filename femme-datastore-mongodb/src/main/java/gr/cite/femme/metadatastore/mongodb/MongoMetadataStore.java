package gr.cite.femme.metadatastore.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.metadata.xpath.MetadataXPath;
import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.ReIndexingProcess;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import gr.cite.femme.client.api.MetadataIndexClient;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;

public class MongoMetadataStore implements MetadataStore {

	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataStore.class);

//	private static final String METADATA_INDEX_HOST = "http://localhost:8083/femme-index-application/metadata-index";
	private MongoMetadataStoreClient mongoMetadataStoreClient;
	private MetadataJsonCollection metadataMongoCollection;
	private MetadataGridFS metadataGridFS;
	private MetadataXPath metadataXPath;

	private final StampedLock lock = new StampedLock();

//	private MetadataIndexClient metadataIndexClient;
	
	/*private XPathCacheManager indexManager;*/
	
	public MongoMetadataStore() {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient();
		this.metadataGridFS = new MetadataGridFS(this.mongoMetadataStoreClient.getMetadataGridFSBucket(), this.mongoMetadataStoreClient.getMetadataGridFSFilesCollection());
		this.metadataXPath = new MetadataXPath();
	}

	public MongoMetadataStore(String host, int port, String name) {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient(host, port, name);
		this.metadataGridFS = new MetadataGridFS(this.mongoMetadataStoreClient.getMetadataGridFSBucket(), this.mongoMetadataStoreClient.getMetadataGridFSFilesCollection());
	}

	public MongoMetadataStore(String host, int port, String name, String bucketName) {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient(host, port, name, bucketName);
		this.metadataGridFS = new MetadataGridFS(this.mongoMetadataStoreClient.getMetadataGridFSBucket(), this.mongoMetadataStoreClient.getMetadataGridFSFilesCollection());
	}
	
	public MongoMetadataStore(String host, int port, String name, String bucketName, MetadataXPath metadataXPath) {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient(host, port, name, bucketName);
//		this.metadataMongoCollection = new MetadataJsonCollection(mongoMetadataStoreClient.getMetadataJson());
		this.metadataGridFS = new MetadataGridFS(this.mongoMetadataStoreClient.getMetadataGridFSBucket(), this.mongoMetadataStoreClient.getMetadataGridFSFilesCollection());
		this.metadataXPath = metadataXPath;
		/*this.indexManager = indexManager;*/
//		metadataIndexClient = new FemmeIndexClient(metadataIndexHost);
	}

	@Override
	public void close() {
		mongoMetadataStoreClient.close();
	}

	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException {
		long stamp = lock.readLock();
		try {
			getMetadataStore(metadatum).insert(metadatum);
			index(metadatum);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public void update(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException {
		this.metadataGridFS.update(metadatum);
	}

	@Override
	public void index(Metadatum metadatum) throws MetadataIndexException {
		if (isXPathable(metadatum)) {
			this.metadataXPath.index(metadatum);
		}
	}

	public void reIndexAll() throws MetadataIndexException, MetadataStoreException {
		MongoCursor<Metadatum> snapshotCursor;
		ReIndexingProcess reIndexer;

		long stamp = lock.writeLock();
		try {
			Instant snapshotTimestamp = Instant.now();
			snapshotCursor = metadataGridFS.findAllBeforeTimestamp(snapshotTimestamp);
			reIndexer = this.metadataXPath.beginReIndexing();
		} finally {
			lock.unlockWrite(stamp);
		}

		List<Future<String>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		while (snapshotCursor.hasNext()) {
			try {
				final Metadatum metadatum = snapshotCursor.next();
				futures.add(executor.submit(() -> {
					try {
						reIndexer.index(metadatum);
					} catch (MetadataIndexException e) {
						throw new RuntimeException(e);
					}
					return metadatum.getId();
				}));
			} catch (RuntimeException e) {
				throw new MetadataIndexException(e);
			}
		}

		for (Future<String> future: futures) {
			try {
				String metadatumId = future.get();
				logger.info("Metadatum " + metadatumId + " successfully indexed");
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage(), e);
			}
		}
		executor.shutdown();

		stamp = lock.writeLock();
		try {
			this.metadataXPath.endReIndexing();
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	@Override
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException {
		return getMetadataStore(metadatum).get(metadatum);
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return find(elementId, false);
	}

	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		
		//List<Metadatum> jsonMetadata = metadataMongoCollection.get(elementId, lazy);
		List<Metadatum> otherMetadata = this.metadataGridFS.find(elementId, lazy);
		
		//metadata.addAll(jsonMetadata);
		metadata.addAll(otherMetadata);
		
		return metadata;
	}

	@Override
	public List<Metadatum> xPath(String xPath) throws MetadataStoreException {
		try {
			return metadataXPath.xPath(xPath);
		} catch (MetadataIndexException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}

	@Override
	public <T extends Element> T xPath(T element, String xPath) throws MetadataStoreException {
		boolean xPathSatisfied = false;
		for (Metadatum metadatum: element.getMetadata()) {
			/*if (getIndexedXPath(metadatum, xPath) != null) {
				xPathSatisfied = true;
			} else {*/
			List<String> xPathResult = getMetadataStore(metadatum).xPath(metadatum, xPath);
			if (xPathResult.size() > 0) {
				xPathSatisfied = true;
				/*indexManager.checkAndCreateIndexOnXPath(metadatum, xPath, xPathResult, element);*/
			}
			/*}*/
		}
		
		return xPathSatisfied ? element : null;
	}
	
	/*private MetadatumXPathCache getIndexedXPath(Metadatum metadatum, String xPath) {
		if (metadatum.getXPathCache() != null) {
			for (MetadatumXPathCache index: metadatum.getXPathCache()) {
				if (index != null && index.getXPath().equals(xPath)) {
					return index;
				}
			}
		}
		
		return null;
	}*/
	/*@Override
	public <T extends Element> List<T> query(List<T> elements, String xPath) throws MetadataStoreException {
		try {
			return elements.stream().filter(new Predicate<T>() {
				@Override
				public boolean test(T t) {
					for (Metadatum metadatum: t.getMetadata()) {
						try {
							if (getMetadataStore(metadatum).xPath(metadatum, xPath) != null) {
								return true;
							}
						} catch (MetadataStoreException e) {
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return false;
				}
			}).collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}*/
	
	@Override
	public void delete(Metadatum metadatum) throws MetadataStoreException {
		getMetadataStore(metadatum).delete(metadatum);
	}
	
	@Override
	public void deleteAll(String elementId) throws MetadataStoreException {
		metadataMongoCollection.deleteAll(elementId);
		metadataGridFS.deleteAll(elementId);
		
	}

	@Override
	public String generateMetadatumId() {
		return new ObjectId().toString();
	}

	private MongoMetadataCollection getMetadataStore(Metadatum metadatum) throws MetadataStoreException {
		if (metadatum.getContentType() != null) {
			if (metadatum.getContentType().toLowerCase().equals("json") || metadatum.getContentType().toLowerCase().contains("json")) {
				return metadataMongoCollection;
			} else {
				return metadataGridFS;
			}
		} else {
			throw new MetadataStoreException("No metadata content type provided");
		}
		
	}
	
	private boolean isIndexable(Metadatum metadatum) {
		return metadatum.getContentType() != null
				&& (metadatum.getContentType().toLowerCase().contains("xml")
				|| metadatum.getContentType().toLowerCase().contains("json"));
	}

	private boolean isXPathable(Metadatum metadatum) {
		return metadatum.getContentType() != null && (metadatum.getContentType().toLowerCase().contains("xml"));
	}

	public void testChangeStatus() throws MetadataStoreException {
		//this.metadataGridFS.changeStatus("", Status.ACTIVE);
		this.mongoMetadataStoreClient.getMetadataGridFSFilesCollection().findOneAndUpdate(Filters.eq(FieldNames.ID, new ObjectId("58e51c629a319213d7cca57b")),
				Updates.set("metadata.status", 20));
	}

	public static void main(String[] args) throws MetadataStoreException {
		MongoMetadataStore store = new MongoMetadataStore();
		store.testChangeStatus();
	}
}
