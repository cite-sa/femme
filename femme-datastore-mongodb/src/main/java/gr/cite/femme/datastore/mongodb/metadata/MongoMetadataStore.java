package gr.cite.femme.datastore.mongodb.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.metadata.xpath.MetadataXPath;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
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
	
//	private MetadataIndexClient metadataIndexClient;
	
	/*private XPathCacheManager indexManager;*/
	
	public MongoMetadataStore() {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient();
		this.metadataGridFS = new MetadataGridFS(mongoMetadataStoreClient.getMetadataGridFS());
	}

	public MongoMetadataStore(String host, String db, String bucketName) {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient(host, db, bucketName);
		this.metadataGridFS = new MetadataGridFS(mongoMetadataStoreClient.getMetadataGridFS());
	}
	
	public MongoMetadataStore(String host, String db, String bucketName, MetadataXPath metadataXPath) {
		this.mongoMetadataStoreClient = new MongoMetadataStoreClient(host, db, bucketName);
//		this.metadataMongoCollection = new MetadataJsonCollection(mongoMetadataStoreClient.getMetadataJson());
		this.metadataGridFS = new MetadataGridFS(mongoMetadataStoreClient.getMetadataGridFS());
		this.metadataXPath = metadataXPath;
		/*this.indexManager = indexManager;*/
//		metadataIndexClient = new FemmeIndexClient(metadataIndexHost);
	}

	@Override
	public void close() {
		mongoMetadataStoreClient.close();
	}

	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException {

		getMetadataStore(metadatum).insert(metadatum);
		if (isXPathable(metadatum)) {
			try {
				metadataXPath.index(metadatum);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new MetadataStoreException("Metadata storing or XPath indexing failed", e);
			} catch (MetadataIndexException e) {
				logger.error(e.getMessage(), e);
				throw new MetadataStoreException("Metadata storing or XPath indexing failed", e);
			} catch (HashGenerationException e) {
				logger.error(e.getMessage(), e);
				throw new MetadataStoreException("Metadata storing or XPath indexing failed", e);
			}
		}

		/*ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Future<String>> futures = new ArrayList<>();

		Future<String> gridFsFuture = executor.submit(() -> {
			getMetadataStore(metadatum).insert(metadatum);
			return metadatum.getId();
		});
		Future<String> xPathFuture = null;
		if (isXPathable(metadatum)) {
			xPathFuture = executor.submit(() -> {
				metadataXPath.index(metadatum);
				return metadatum.getId();
			});
		}

		// TODO rollback in case of error
		try {
			gridFsFuture.get();
			if (xPathFuture != null) {
				xPathFuture.get();
			}
		} catch (Exception e) {
			executor.shutdown();
			logger.error(e.getMessage(), e);
			throw new MetadataStoreException("Metadata storing or XPath indexing failed", e);
		} finally {
			executor.shutdown();
		}*/
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
		
		//List<Metadatum> jsonMetadata = metadataMongoCollection.find(elementId, lazy);
		List<Metadatum> otherMetadata = metadataGridFS.find(elementId, lazy);
		
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
		if (metadatum.getContentType() != null 
				&&(metadatum.getContentType().toLowerCase().contains("xml") 
						|| metadatum.getContentType().toLowerCase().contains("json"))) {
			return true;
		}
		return false;
	}

	private boolean isXPathable(Metadatum metadatum) {
		if (metadatum.getContentType() != null &&(metadatum.getContentType().toLowerCase().contains("xml"))) {
			return true;
		}
		return false;
	}
}
