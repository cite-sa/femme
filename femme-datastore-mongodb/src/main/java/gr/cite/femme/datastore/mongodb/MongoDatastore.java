package gr.cite.femme.datastore.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.query.api.QueryExecutor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoGridFSException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.api.MetadataStore;
/*import gr.cite.femme.datastore.mongodb.cache.MongoXPathCacheManager;*/
import gr.cite.femme.datastore.mongodb.utils.Documentizer;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.query.mongodb.CriterionMongo;
import gr.cite.femme.query.mongodb.QueryMongo;
import gr.cite.femme.query.mongodb.QueryOptionsBuilderMongo;

public class MongoDatastore implements Datastore<CriterionMongo, QueryMongo>  {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	private MongoDatastoreClient mongoClient;
	
	private MongoCollection<Collection> collections;
	
	private MongoCollection<DataElement> dataElements;
	
	private MetadataStore metadataStore;
	

	public MongoDatastore() {
		this.mongoClient = new MongoDatastoreClient();
		this.collections = mongoClient.getCollections();
		this.dataElements = mongoClient.getDataElements();
//		this.metadataStore = new MongoMetadataStore(mongoClient);
	}

	public MongoDatastore(String dbHost, String dbName) {
		this.mongoClient = new MongoDatastoreClient(dbHost, dbName);
		this.collections = mongoClient.getCollections();
		this.dataElements = mongoClient.getDataElements();

//		this.metadataStore = new MongoMetadataStore(mongoClient);
	}

	public MongoDatastore(String dbHost, String dbName, MetadataStore metadataStore) {
		this.mongoClient = new MongoDatastoreClient(dbHost, dbName);
		this.collections = mongoClient.getCollections();
		this.dataElements = mongoClient.getDataElements();

		this.metadataStore = metadataStore;
	}

	public MongoCollection<Collection> getCollections() {
		return collections;
	}

	public MongoCollection<DataElement> getDataElements() {
		return dataElements;
	}

	@Override
	public void close() {
		this.mongoClient.close();
	}

	public MetadataStore getMetadataStore() {
		return metadataStore;
	}
	
	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String elementId) {
		for (Metadatum metadatum : metadata) {
			try {
				metadatum.setElementId(elementId);
				metadataStore.insert(metadatum);
			} catch (MetadataStoreException e) {
				logger.error("Element " + elementId + " metadatum insertion failed", e);
				//throw new DatastoreException("Element " + elementId + "metadatum insertion failed.");
			} catch (MetadataIndexException e) {
				logger.warn("Element " + elementId + " metadatum indexing failed", e);
			}
		}
		return metadata;
	}

	@Override
	public String insert(Element element) throws DatastoreException {
		element.setId(new ObjectId().toString());
		insertMetadata(element.getMetadata(), element.getId());

		try {
			if (element instanceof Collection) {
				insertCollection((Collection) element);
			} else if (element instanceof DataElement) {
				insertDataElement((DataElement)element);
				/*if (dataElement.getCollections().size() > 0) {
					collections.insertMany(dataElement.getCollections());
				}*/
			}
		} catch (DatastoreException e) {
			try {
				metadataStore.deleteAll(element.getId());
			} catch (MetadataStoreException e1) {
				logger.error(e1.getMessage(), e1);
			}
			throw e;
		}
		return element.getId();
	}
	
	private void insertCollection(Collection collection) throws DatastoreException {
		Instant now = Instant.now();
		
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
			// Duplicate key error. Collection already exists
			if (11000 == e.getCode()) {
				logger.info("Collection " + collection.getName() + " already exists. " + e.getMessage());
			} else {
				logger.info("Collection " + collection.getName() + " insertion failed. ", e);
				throw new DatastoreException("Collection " + collection.getName() + " insertion failed. " + e.getMessage(), e);				
			}
			
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
	}
	
	private void insertDataElement(DataElement dataElement) throws DatastoreException {
		
		Instant now = Instant.now();
		dataElement.getSystemicMetadata().setCreated(now);
		dataElement.getSystemicMetadata().setModified(now);
		
		try {
			dataElements.insertOne(dataElement);
		} catch (MongoException e) {
			throw new DatastoreException("DataElement" + dataElement.getEndpoint() + " insertion failed", e);
		}
		
		
		/*UpdateResult updateResult = null;
		try {
			updateResult = dataElements.updateOne(
					new Document().append(FieldNames.ID, new ObjectId(dataElement.getId())),
					new Document().append("$set", dataElement),
					new UpdateOptions().upsert(true));
		} catch (MongoWriteException e1) {
			throw new DatastoreException("DataElement" + dataElement.getEndpoint() + " insertion failed", e1);
		}  catch (MongoWriteConcernException e2) {
			throw new DatastoreException("DataElement" + dataElement.getEndpoint() + " insertion failed", e2);
		}  catch(MongoException e3) {
			throw new DatastoreException("DataElement" + dataElement.getEndpoint() + " insertion failed", e3);
		}
		logger.info(updateResult.toString());*/
		
	}

	public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException {
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
				 * if (dataElement.getCollections() != null && dataElement.getCollections().size() > 0) {
				 * 		collections.insertMany(dataElement.getCollections());
				 * }
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

		return elements.stream().map(element -> element.getId()).collect(Collectors.toList());
	}

	@Override
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException {
		return addToCollection(dataElement,
				QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end()));
	}
	
	@Override
	public DataElement addToCollection(DataElement dataElement, QueryMongo query) throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + query.build());

		/*if (dataElement.getId() == null) {
			dataElement.setId(new ObjectId().toString());
		}*/

		/*FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		
		updatedCollection = collections
				.findOneAndUpdate(
						query.build(),
						new Document().append("$addToSet",
								new Document().append("dataElements", Documentizer.toIdNameDocument(dataElement))),
						options);*/
		Collection collection = collections.find(query.build()).limit(1).first();

		/*if (updatedCollection != null) {*/
		if (collection != null) {
			/*dataElement.addCollection(updatedCollection);*/
			Collection dataElementCollection = new Collection();
			dataElementCollection.setId(collection.getId());
			dataElementCollection.setName(collection.getName());
			dataElementCollection.setEndpoint(collection.getEndpoint());
			dataElement.setCollections(Arrays.asList(collection));
			
			insert(dataElement);
		} else {
			logger.info("No collection updated");
		}
		return dataElement;
	}

	@Override
	public Collection addToCollection(List<DataElement> dataElementsList, QueryMongo query) throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + query.build());

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
						query.build(),
						new Document()
							.append("$addToSet",
								new Document().append("dataElements",
									new Document().append("$each", dataElementDocuments))),
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
	public <T extends Element> String update(T element) throws DatastoreException {
		String id = null;
		
		if (element instanceof Collection) {
			id = updateCollection((Collection) element);
		} else if (element instanceof DataElement){
			id = updateDataElement((DataElement) element);
		}
		return id;
	}
	
	private String updateCollection(Collection collection) {
		return null;
	}
	
	private String updateDataElement(DataElement dataElement) {
		return null;
	}

	@Override
	public <T extends Element> void delete(QueryMongo query, Class<T> elementSubtype) throws DatastoreException {
		int deletedCount = 0;
		String subtype = elementSubtype.getSimpleName();

		logger.debug("Delete criteria query: " + query);
		
		List<T> toBeDeleted = find(query, elementSubtype).list();
		
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
	public <T extends Element> Element query(String id, Class<T> elementSubtype) throws IllegalElementSubtype {
		String subtype = elementSubtype.getSimpleName();
		if (subtype.equals("DataElement")) {
			return dataElements.query(new Document().append(FieldNames.ID, id)).limit(1).first();
		} else if (subtype.equals("Collection")) {
			return collections.query(new Document().append(FieldNames.ID, id)).limit(1).first();
		} else {
			throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype.");
		}

	}*/
	
	@Override
	public Collection getCollection(String id) throws DatastoreException {
		Collection collection;
		collection = collections.find(Filters.eq(FieldNames.ID, new ObjectId(id))).limit(1).first();
		if (collection != null) {			
			collection.setMetadata(collection != null ? getMetadata(collection): new ArrayList<Metadatum>());
		}
		return collection;
	}
	
	@Override
	public DataElement getDataElement(String id) throws DatastoreException {
		DataElement dataElement;
		dataElement = dataElements.find(Filters.eq(FieldNames.ID, new ObjectId(id))).limit(1).first();
		if (dataElement != null) {
			dataElement.setMetadata(getMetadata(dataElement));
		}
		return dataElement;
	}
	
	@Override
	public DataElement getDataElementByName(String name) throws DatastoreException {
		DataElement dataElement;
		dataElement = dataElements.find(Filters.eq(FieldNames.NAME, name)).limit(1).first();
		if (dataElement != null) { 
			dataElement.setMetadata(getMetadata(dataElement));
		}
		return dataElement;
	}

	@Override
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		QueryMongo queryMongo = (QueryMongo) query;
		return new QueryOptionsBuilderMongo<T>().query(this, elementSubtype).find(queryMongo);
	}
	
	@Override
	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return new QueryOptionsBuilderMongo<T>().count((QueryMongo)query, this, elementSubtype);
	}
	

	@Override
	public void remove(DataElement dataElement, Collection collection) throws DatastoreException {

	}
	
	public <T extends Element> List<Metadatum> getMetadata(T element) throws DatastoreException {
		try {
			return metadataStore.find(element.getId(), false);
		} catch (MetadataStoreException e) {
			throw new DatastoreException("Error during metadatum retrieval", e);
		}
		/*try {
			return metadataStore.get(element.getId());
		} catch(MetadataStoreException e) {
			throw new DatastoreException("Error during metadatum retrieval", e);
		}*/
	}

}
