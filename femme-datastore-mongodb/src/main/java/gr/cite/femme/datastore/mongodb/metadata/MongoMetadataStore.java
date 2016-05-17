package gr.cite.femme.datastore.mongodb.metadata;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJson;

public class MongoMetadataStore implements MetadataStore {
	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataStore.class);
	
	private MetadataMongoCollection metadataMongoCollection;
	
	private MetadataGridFS metadataGridFS;
	
	public MongoMetadataStore() {
		
	}
	
	public MongoMetadataStore(MongoCollection<MetadatumJson> metadataMongoCollection, GridFSBucket metadataGridFS) {
		this.metadataMongoCollection = new MetadataMongoCollection(metadataMongoCollection);
		this.metadataGridFS = new MetadataGridFS(metadataGridFS);
	}

	@Override
	public String insert(Metadatum metadatum) throws MetadataStoreException {
		return getMetadataStore(metadatum).insert(metadatum);
	}

	@Override
	public Metadatum get(String fileId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Element> List<T> find(List<T> elements, String xPath) throws MetadataStoreException {
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
	}
	
	@Override
	public Metadatum xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		return getMetadataStore(metadatum).xPath(metadatum, xPath);
	}

	@Override
	public List<Metadatum> find(Metadatum metadatum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metadatum> find(List<Metadatum> metadataList) throws MetadataStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Metadatum metadatum) throws MetadataStoreException {
		getMetadataStore(metadatum).delete(metadatum);
	}
	
	@Override
	public void delete(String elementId) throws MetadataStoreException {
		metadataMongoCollection.delete(elementId);
		metadataGridFS.delete(elementId);
		
	}

	private MetadataStore getMetadataStore(Metadatum metadatum) throws MetadataStoreException {
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
}
