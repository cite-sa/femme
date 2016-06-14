package gr.cite.femme.datastore.mongodb;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoGridFSException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.IllegalElementSubtype;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.cache.XPathCacheManager;
import gr.cite.femme.datastore.mongodb.cache.MongoXPathCacheManager;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.datastore.mongodb.metadata.MongoMetadataStore;
import gr.cite.femme.datastore.mongodb.utils.Documentizer;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IQuery;
import gr.cite.femme.query.IQueryOptions;
import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Query;
import gr.cite.femme.query.mongodb.QueryOptions;

public class MongoDatastore implements Datastore<Criteria, Query>  {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	MongoDatastoreClient mongoClient;
	MongoCollection<Collection> collections;
	MongoCollection<DataElement> dataElements;
	MetadataStore metadataStore;

	public MongoDatastore() {
		mongoClient = new MongoDatastoreClient();
		collections = mongoClient.getCollections();
		dataElements = mongoClient.getDataElements();
		metadataStore = new MongoMetadataStore(mongoClient.getMetadataJson(), mongoClient.getMetadataGridFS(), new MongoXPathCacheManager(this));
	}

	public MongoDatastore(String dbHost, String dbName) {
		mongoClient = new MongoDatastoreClient(dbHost, dbName);
		collections = mongoClient.getCollections();
		dataElements = mongoClient.getDataElements();
		metadataStore = new MongoMetadataStore(mongoClient.getMetadataJson(), mongoClient.getMetadataGridFS(), new MongoXPathCacheManager(this));
	}

	public MongoCollection<Collection> getCollections() {
		return collections;
	}

	public MongoCollection<DataElement> getDataElements() {
		return dataElements;
	}

	public void close() {
		mongoClient.close();
	}

	public MetadataStore getMetadataStore() {
		return metadataStore;
	}
	
	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String elementId) throws DatastoreException {
		for (Metadatum metadatum : metadata) {
			try {
				metadatum.setElementId(elementId.toString());
				metadataStore.insert(metadatum);
			} catch (MetadataStoreException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException("Inserting element metadata failed.");
			}
		}
		return metadata;
	}

	@Override
	public <T extends Element> T insert(T element) throws DatastoreException {
		Instant now = Instant.now();
		element.setId(new ObjectId().toString());
		insertMetadata(element.getMetadata(), element.getId());

		try {
			if (element instanceof Collection) {
				Collection collection = (Collection) element;

				for (DataElement dataElement : collection.getDataElements()) {
					dataElement.addCollection(collection);
					if (dataElement.getId() == null) {
						dataElement.setId(new ObjectId().toString());
					}
				}

				try {
					collection.getSystemicMetadata().setCreated(now);
					collection.getSystemicMetadata().setModified(now);
					collections.insertOne(collection);
				} catch (MongoException e) {
					throw new DatastoreException("Collection " + collection.getName() + " insertion failed.", e);
				}

				for (DataElement dataElement : collection.getDataElements()) {
					try {
						insertMetadata(dataElement.getMetadata(), dataElement.getId());
					} catch (MongoGridFSException e) {
						logger.error(e.getMessage(), e);
					}
				}
				if (collection.getDataElements().size() > 0) {
					for (DataElement dataElement: collection.getDataElements()) {
						dataElement.getSystemicMetadata().setCreated(now);
						dataElement.getSystemicMetadata().setModified(now);
					}
					dataElements.insertMany(collection.getDataElements());

				}
			} else if (element instanceof DataElement) {
				DataElement dataElement = (DataElement) element;
				dataElement.getSystemicMetadata().setCreated(now);
				dataElement.getSystemicMetadata().setModified(now);
				dataElements.insertOne(dataElement);

				/*if (dataElement.getCollections().size() > 0) {
					collections.insertMany(dataElement.getCollections());
				}*/
			}
		} catch (MongoException e) {
			try {
				metadataStore.deleteAll(element.getId());
			} catch (MetadataStoreException e1) {
				logger.error(e1.getMessage(), e1);
			}
			throw new DatastoreException("Inserting collection failed.", e);
		}
		return element;
	}

	public <T extends Element> List<T> insert(List<T> elements) throws DatastoreException {
		for (Element element : elements) {
			if (element.getId() != null) {
				element.setId(new ObjectId(element.getId()).toString());
			} else {
				element.setId(new ObjectId().toString());
			}
			try {
				insertMetadata(element.getMetadata(), element.getId());
			} catch (MongoGridFSException e) {
				throw new DatastoreException("Bulk insertion failed.", e);
			}
		}

		try {
			if (elements.get(0) instanceof Collection) {
				List<Collection> collectionList = (List<Collection>) elements;

				try {
					collections.insertMany(collectionList);
				} catch (MongoException e) {
					throw new DatastoreException("Collection bulk insertion failed.", e);
				}

				for (Collection collection : collectionList) {
					for (DataElement dataElement : collection.getDataElements()) {
						dataElement.addCollection(collection);
						dataElement.setId(new ObjectId().toString());
						try {
							insertMetadata(dataElement.getMetadata(), dataElement.getId());
						} catch (MongoGridFSException e) {
							logger.error(e.getMessage(), e);
						}
					}
					if (collection.getDataElements() != null && collection.getDataElements().size() > 0) {
						dataElements.insertMany(collection.getDataElements());
					}
				}
			} else if (elements.get(0) instanceof DataElement) {
				List<DataElement> dataElementList = (List<DataElement>) elements;
				dataElements.insertMany(dataElementList);

				/*
				 * if (dataElement.getCollections() != null &&
				 * dataElement.getCollections().size() > 0) {
				 * collections.insertMany(dataElement.getCollections()); }
				 */
			}
		} catch (MongoException e) {
			for (Element element : elements) {
				try {
					metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
			throw new DatastoreException("Bulk insertion failed.", e);
		}

		return elements;
	}

	@Override
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException {
		try {
			return addToCollection(dataElement, Criteria.query().where(FieldNames.ID).eq(new ObjectId(collectionId)));
		} catch (InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException(e.getMessage(), e);
		}
	}
	
	@Override
	public DataElement addToCollection(DataElement dataElement, ICriteria criteria) throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + new Document(criteria.getCriteria()).toJson().toString());

		if (dataElement.getId() == null) {
			dataElement.setId(new ObjectId().toString());
		}

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		updatedCollection = collections
				.findOneAndUpdate(
						BsonDocument
								.parse(new Document(
										criteria.getCriteria())
												.toJson()),
						BsonDocument.parse(new Document().append("$addToSet",
								new Document().append("dataElements", Documentizer.toOnlyIdDocument(dataElement)))
								.toJson()),
						options);

		if (updatedCollection != null) {
			dataElement.addCollection(updatedCollection);
			insert(dataElement);
		} else {
			logger.info("No collection updated");
		}
		return dataElement;
	}

	@Override
	public Collection addToCollection(List<DataElement> dataElementsList, ICriteria criteria)
			throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + new Document(criteria.getCriteria()).toJson().toString());

		List<Document> dataElementDocuments = dataElementsList.stream().map(new Function<DataElement, Document>() {
			@Override
			public Document apply(DataElement dataElement) {
				if (dataElement.getId() == null) {
					dataElement.setId(new ObjectId().toString());
				}
				return Documentizer.toOnlyIdDocument(dataElement);
			}
		}).collect(Collectors.toList());

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		updatedCollection = collections
				.findOneAndUpdate(
						BsonDocument
								.parse(new Document(
										criteria.getCriteria())
												.toJson()),
						BsonDocument
								.parse(new Document()
										.append("$addToSet",
												new Document().append("dataElements",
														new Document().append("$each", dataElementDocuments)))
										.toJson()),
						options);

		if (updatedCollection != null) {
			for (DataElement dataElement : dataElementsList) {
				dataElement.addCollection(updatedCollection);
			}
			insert(dataElementsList);
		} else {
			logger.info("No Collection updated");
		}
		return updatedCollection;
	}

	@Override
	public Element update(Element element) throws DatastoreException {
		return null;
	}

	@Override
	public <T extends Element> void delete(Criteria criteria, Class<T> elementSubtype) throws DatastoreException {
		int deletedCount = 0;
		String subtype = elementSubtype.getSimpleName();

		logger.debug("Delete criteria query: " + new Document(criteria.getCriteria()).toJson().toString());
		
		List<T> toBeDeleted = find(new Query(criteria), elementSubtype).list();
		
		for (T element: toBeDeleted) {
			if (element instanceof DataElement) {
				try {
					logger.debug("Delete DataElement " + element.getId());
					dataElements.deleteOne(Filters.eq(FieldNames.ID, new ObjectId(element.getId())));
					logger.debug("Delete completed");
					deletedCount++;
				} catch(MongoException e) {
					throw new DatastoreException("Couldn't delete DataElement " + element.getId(), e);
				}
				// TODO: remove DataElement id from Collection
				try {
					metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e) {
					logger.error(e.getMessage(), e);
				}
			} else if (element instanceof Collection) {
				try {
					logger.debug("Delete Collection " + element.getId());
					collections.deleteOne(Filters.eq(FieldNames.ID, element.getId()));
					logger.debug("Delete completed");
					deletedCount++;
				} catch(MongoException e) {
					throw new DatastoreException("Couldn't delete Collection " + element.getId(), e);
				}
				// TODO: remove Collection id from DataElements
				try {
					metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		/*try {
			if (subtype.equals("DataElement")) {
				result = dataElements.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else if (subtype.equals("Collection")) {
				result = collections.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else {
				throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype");
			}
		} catch(MongoException e) {
			throw new DatastoreException("Couldn't delete element(s)", e);
		}*/

		if (deletedCount == toBeDeleted.size()) {
			logger.info("All " + deletedCount + " elements of type " + subtype + " were successfully deleted");
		} else if (deletedCount < toBeDeleted.size()) {
			logger.info(toBeDeleted.size() + " of " + deletedCount + " elements of type " + subtype + " were successfully deleted");			
		}
		
	}

	/*@Override
	public <T extends Element> Element find(String id, Class<T> elementSubtype) throws IllegalElementSubtype {
		String subtype = elementSubtype.getSimpleName();
		if (subtype.equals("DataElement")) {
			return dataElements.find(new Document().append(FieldNames.ID, id)).limit(1).first();
		} else if (subtype.equals("Collection")) {
			return collections.find(new Document().append(FieldNames.ID, id)).limit(1).first();
		} else {
			throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype.");
		}

	}*/
	
	@Override
	public Collection getCollection(String id) {
		return collections.find(Filters.eq(FieldNames.ID, new ObjectId(id))).limit(1).first();
	}
	
	@Override
	public DataElement getDataElement(String id) {
		return dataElements.find(Filters.eq(FieldNames.ID, new ObjectId(id))).limit(1).first();
	}

	@Override
	public <T extends Element> IQueryOptions<T> find(Query query, Class<T> elementSubtype) {
		return new QueryOptions<T>(query, this, elementSubtype);
	}

	@Override
	public void remove(Element dataElement, Collection collection) throws DatastoreException {

	}

}
