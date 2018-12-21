package gr.cite.femme.engine.metadatastore.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCursor;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.engine.metadata.xpath.MetadataXPath;
import gr.cite.femme.engine.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class MongoMetadataStore implements MetadataStore {
	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataStore.class);

	private MetadatastoreRepository metadatastoreRepository;
	private MetadataJsonCollection metadataMongoCollection;
	//private MetadataGridFSRepository metadataGridFSRepository;
	private MetadataXPath metadataXPath;

	private final StampedLock lock = new StampedLock();
	
	@Inject
	public MongoMetadataStore(MetadatastoreRepository metadatastoreRepository, MetadataXPath metadataXPath) {
		this.metadatastoreRepository = metadatastoreRepository;
		//this.metadataGridFSRepository = new MetadataGridFSRepository(this.metadatastoreRepository.getMetadataGridFSBucket(), this.metadatastoreRepository.getMetadataGridFSFilesCollection());
		this.metadataXPath = metadataXPath;
	}
	
	private boolean isFemmeInIndexMode() {
		return this.metadataXPath != null;
	}

	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException {
		long stamp = lock.readLock();
		try {
			this.metadatastoreRepository.insert(metadatum);
			index(metadatum);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public Metadatum update(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException {
		Metadatum updatedMetadatum = this.metadatastoreRepository.update(metadatum);

		if (updatedMetadatum != null) {
			index(metadatum);
			deIndex(metadatum);
		}
		return updatedMetadatum;
	}

	/*@Override
	public Metadatum update(String id, Map<String, Object> fieldsAndValues) throws MetadataStoreException, MetadataIndexException {
		return this.metadataGridFSRepository.update(id, fieldsAndValues);
	}*/

	/*@Override
	public void index(Metadatum metadatum) throws MetadataIndexException {
	
	}

	@Override
	public void deIndexMetadatum(String id) throws MetadataIndexException {
		if (isFemmeInIndexMode()) {
			this.metadataXPath.deIndex(id);
		}
	}

	@Override
	public void deIndexElement(String elementId) throws MetadataIndexException {
		if (isFemmeInIndexMode()) {
			this.metadataXPath.deIndexAll(elementId);
		}
	}*/

	@Override
	public void reIndexAll() throws MetadataIndexException, MetadataStoreException {
		if (isFemmeInIndexMode()) {
			MongoCursor<Metadatum> snapshotCursor;
			ReIndexingProcess reIndexer;

			long stamp = lock.writeLock();
			try {
				Instant snapshotTimestamp = Instant.now();
				snapshotCursor = this.metadatastoreRepository.findAllBeforeTimestamp(snapshotTimestamp);
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

			for (Future<String> future : futures) {
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
	}

	@Override
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException {
		return get(metadatum, false);
	}

	@Override
	public Metadatum get(Metadatum metadatum, boolean lazy) throws MetadataStoreException {
		return this.metadatastoreRepository.get(metadatum.getId(), lazy);
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return find(elementId, false);
	}

	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		return find(elementId, lazy, false);
	}

	@Override
	public List<Metadatum> find(String elementId, boolean lazy, boolean loadInactive) throws MetadataStoreException {
		return this.metadatastoreRepository.find(elementId, lazy, loadInactive);
	}

	@Override
	public List<Metadatum> xPath(String xPath, boolean lazyPayload) throws MetadataStoreException {
		if (isFemmeInIndexMode()) {
			return xPath(new ArrayList<>(), xPath, lazyPayload);
		} else {
			throw new MetadataStoreException("FeMME not in index mode");
		}
	}
	
	@Override
	public List<Metadatum> xPath(List<String> elementIds, String xPath, boolean lazyPayload) throws MetadataStoreException {
		if (isFemmeInIndexMode()) {
			try {
				return this.metadataXPath.xPath(elementIds, xPath, lazyPayload);
			} catch (MetadataIndexException e) {
				throw new MetadataStoreException(e.getMessage(), e);
			}
		} else {
			throw new MetadataStoreException("FeMME not in index mode");
		}
	}
	
	@Override
	public List<Metadatum> xPathInMemory(String xPath) throws MetadataStoreException {
		return xPathInMemory(new ArrayList<>(), xPath);
	}

	@Override
	public List<Metadatum> xPathInMemory(List<String> elementIds, String xPath) throws MetadataStoreException {
		List<Metadatum> xPathedMetadata = new ArrayList<>();
		List<Metadatum> metadata = this.metadatastoreRepository.find(elementIds, false);
		
		for (Metadatum metadatum: metadata) {
			if (metadatum.getValue() != null) {
				try {
					String xPathResult = new XPathEvaluator(XMLConverter.stringToNode(metadatum.getValue())).evaluate(xPath).stream().collect(Collectors.joining());
					
					if (xPathResult.length() > 0) {
						metadatum.setValue(xPathResult);
						xPathedMetadata.add(metadatum);
					}
				} catch (XPathEvaluationException | XMLConversionException | XPathFactoryConfigurationException e) {
					logger.error("In memory XPath: " + e.getMessage());
				}
			}
		}
		
		return xPathedMetadata;
	}
	
	@Override
	public void delete(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException {
		deIndex(metadatum);
		this.metadatastoreRepository.delete(metadatum.getId());
	}
	
	public void delete(List<Metadatum> metadata) throws MetadataStoreException, MetadataIndexException {
		for (Metadatum metadatum: metadata) {
			try {
				deIndex(metadatum);
				this.metadatastoreRepository.delete(metadatum.getId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void delete(Element element) throws MetadataStoreException {
		deIndexAll(element);
		//metadataMongoCollection.deleteAll(elementId);
		this.metadatastoreRepository.deleteAll(element.getId());
	}
	
	@Override
	public void softDelete(String metadatumId) throws MetadataStoreException {
		this.metadatastoreRepository.updateStatus(metadatumId, Status.INACTIVE);
	}

	@Override
	public void softDeleteAll(String elementId) throws MetadataStoreException {
		List<Metadatum> metadata = this.metadatastoreRepository.find(elementId, true);
		for (Metadatum metadatum: metadata) {
			this.metadatastoreRepository.updateStatus(metadatum.getId(), Status.INACTIVE);
		}
	}

	@Override
	public String generateMetadatumId() {
		return new ObjectId().toString();
	}
	
	private void index(Metadatum metadatum) {
		if (isFemmeInIndexMode()) {
			try {
				this.metadataXPath.index(metadatum);
			} catch (MetadataIndexException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void deIndex(Metadatum metadatum) {
		if (isFemmeInIndexMode()) {
			try {
				this.metadataXPath.deIndex(metadatum.getId());
			} catch (MetadataIndexException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void deIndexAll(Element element) {
		if (isFemmeInIndexMode()) {
			try {
				this.metadataXPath.deIndexAll(element.getId());
			} catch (MetadataIndexException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
