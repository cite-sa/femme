package gr.cite.femme.engine.datastore.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import gr.cite.femme.engine.query.execution.mongodb.QueryExecutorFactory;
import gr.cite.femme.core.query.execution.QueryExecutor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.datastores.Datastore;
/*import gr.cite.femme.datastore.mongodb.cache.MongoXPathCacheManager;*/
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;

public class MongoDatastore implements Datastore {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	private MongoDatastoreClient mongoClient;
	/*private MetadataStore metadataStore;*/

	public MongoDatastore() {
		this.mongoClient = new MongoDatastoreClient();
		/*this.metadataStore = new MongoMetadataStore();*/
	}

	public MongoDatastore(String host, int port, String name) {
		this.mongoClient = new MongoDatastoreClient(host, port, name);
		/*this.metadataStore = new MongoMetadataStore();*/
	}

	public <T extends Element> MongoCollection<T> getCollection(Class<T> elementSubType) {
		return this.mongoClient.getCollection(elementSubType);
	}

	public MongoCollection<Collection> getCollections() {
		return this.mongoClient.getCollections();
	}

	public MongoCollection<DataElement> getDataElements() {
		return this.mongoClient.getDataElements();
	}

	@Override
	public void close() {
		this.mongoClient.close();
	}

	/*public MetadataStore getMetadataStore() {
		return this.metadataStore;
	}

	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String elementId) {
		for (Metadatum metadatum : metadata) {
			try {
				metadatum.setElementId(elementId);
				this.metadataStore.insert(metadatum);
			} catch (MetadataStoreException e) {
				logger.error("Element " + elementId + " metadatum insertion failed", e);
				//throw new DatastoreException("Element " + elementId + "metadatum insertion failed.");
			} catch (MetadataIndexException e) {
				logger.warn("Element " + elementId + " metadatum indexing failed", e);
			}
		}
		return metadata;
	}*/

	@Override
	public String insert(Element element) throws DatastoreException {
		/*element.setId(new ObjectId().toString());
		insertMetadata(element.getMetadata(), element.getId());*/
		Instant now = Instant.now();
		element.getSystemicMetadata().setCreated(now);
		element.getSystemicMetadata().setModified(now);

		if (element instanceof Collection) {
			try {
				this.mongoClient.getCollections().insertOne((Collection) element);
			} catch (MongoException e) {
				// Duplicate key error. Collection already exists
				if (11000 == e.getCode()) {
					logger.info("Collection " + element.getEndpoint() + " already exists. " + e.getMessage());
				} else {
					throw new DatastoreException("Collection " + element.getName() + " insertion failed", e);
				}
			}
		} else if (element instanceof DataElement) {
			try {
				this.mongoClient.getDataElements().insertOne((DataElement) element);
			} catch (MongoException e) {
				throw new DatastoreException("DataElement " + element.getName() + " insertion failed", e);
			}
		}
		return element.getId();
	}

	@Override
	public List<String> insert(List<? extends Element> elements) throws DatastoreException {
		if (elements != null && elements.size() > 0) {
			elements.forEach(element -> {
				Instant now = Instant.now();
				element.getSystemicMetadata().setCreated(now);
				element.getSystemicMetadata().setModified(now);
			});

			if (elements.get(0) instanceof Collection) {
				List<Collection> collections = (List<Collection>) elements;
				try {
					this.mongoClient.getCollections().insertMany(collections);
				} catch (MongoException e) {
					throw new DatastoreException("Collection bulk insertion failed.", e);
				}
			} else if (elements.get(0) instanceof DataElement) {
				List<DataElement> dataElements = (List<DataElement>) elements;
				try {
					this.mongoClient.getDataElements().insertMany(dataElements);
				} catch (MongoException e) {
					throw new DatastoreException("DataElement bulk insertion failed.", e);
				}
			}
		}

		return elements != null && elements.size() > 0 ? elements.stream().map(Element::getId).collect(Collectors.toList()) : new ArrayList<>();
	}
	
	/*private void insertCollection(Collection collection) throws DatastoreException {
		Instant now = Instant.now();
		
		*//*for (DataElement dataElement : collection.getDataElements()) {
			dataElement.addCollection(collection);
			if (dataElement.getId() == null) {
				dataElement.setId(new ObjectId().toString());
			}
		}*//*

		try {
			collection.getSystemicMetadata().setCreated(now);
			collection.getSystemicMetadata().setModified(now);
			this.mongoClient.getCollections().insertOne(collection);
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
				*//*insertMetadata(dataElement.getMetadata(), dataElement.getId());*//*
			} catch (MongoGridFSException e) {
				logger.error(e.getMessage(), e);
			}
		}
		if (collection.getDataElements().size() > 0) {
			for (DataElement dataElement: collection.getDataElements()) {
				dataElement.getSystemicMetadata().setCreated(now);
				dataElement.getSystemicMetadata().setModified(now);
			}
			this.mongoClient.getDataElements().insertMany(collection.getDataElements());

		}
	}*/
	
	/*private void insertDataElement(DataElement dataElement) throws DatastoreException {
		
		Instant now = Instant.now();
		dataElement.getSystemicMetadata().setCreated(now);
		dataElement.getSystemicMetadata().setModified(now);
		
		try {
			this.mongoClient.getDataElements().insertOne(dataElement);
		} catch (MongoException e) {
			throw new DatastoreException("DataElement" + dataElement.getEndpoint() + " insertion failed", e);
		}
		
		
		*//*UpdateResult updateResult = null;
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
		logger.info(updateResult.toString());*//*
		
	}*/

	/*public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException {
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
					this.mongoClient.getCollections().insertMany(collectionList);
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
						this.mongoClient.getDataElements().insertMany(collection.getDataElements());
					}
				}
			} else if (elements.get(0) instanceof DataElement) {
				List<DataElement> dataElementList = (List<DataElement>) elements;
				this.mongoClient.getDataElements().insertMany(dataElementList);

				*//*
				 * if (dataElement.getCollections() != null && dataElement.getCollections().size() > 0) {
				 * 		collections.insertMany(dataElement.getCollections());
				 * }
				 *//*
			}
		} catch (MongoException e) {
			for (Element element : elements) {
				try {
					this.metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
			throw new DatastoreException("Bulk insertion failed.", e);
		}

		return elements.stream().map(element -> element.getId()).collect(Collectors.toList());
	}*/

	@Override
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException {
		return addToCollection(dataElement, QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end()));
	}

	@Override
	public DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException {
		QueryMongo mongoQuery = (QueryMongo) query;
		logger.debug("addToCollection criteria getQueryExecutor: " + mongoQuery.build());
		//find(getQueryExecutor, Collection.class).first()
		Collection collection = this.mongoClient.getCollections().find(mongoQuery.build()).limit(1).first();

		if (collection != null) {
			/*Collection dataElementCollection = new Collection();
			dataElementCollection.setId(collection.getId());
			dataElementCollection.setName(collection.getName());
			dataElementCollection.setEndpoint(collection.getEndpoint());*/
			dataElement.setCollections(Collections.singletonList(collection));

			insert(dataElement);
		} else {
			logger.info("No collection updated");
		}
		return dataElement;
	}

	/*@Override
	public DataElement addToCollection(DataElement dataElement, QueryMongo getQueryExecutor) throws DatastoreException {
		logger.debug("addToCollection criteria getQueryExecutor: " + getQueryExecutor.execute());
		Collection collection = this.mongoClient.getCollections().get(getQueryExecutor.execute()).limit(1).first();

		if (collection != null) {
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
	}*/

	/*@Override
	public Collection addToCollection(List<DataElement> dataElementsList, QueryMongo getQueryExecutor) throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria getQueryExecutor: " + getQueryExecutor.execute());

		List<Document> dataElementDocuments = dataElementsList.stream().map(dataElement -> {
			if (dataElement.getId() == null) {
				dataElement.setId(new ObjectId().toString());
			}
			return Documentizer.toOnlyIdDocument(dataElement);
		}).collect(Collectors.toList());

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		updatedCollection = this.mongoClient.getCollections().findOneAndUpdate(
				getQueryExecutor.execute(),
				new Document().append("$addToSet", new Document().append("dataElements", new Document().append("$each", dataElementDocuments))),
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
	}*/

	@Override
	public Element update(Element element) throws DatastoreException {
		Element updated = null;

		if (element.getId() != null) {
			if (element.getSystemicMetadata() != null) {
				element.getSystemicMetadata().setModified(Instant.now());
			}

			//Document forUpdate = Documentizer.toDocument(element);
			updated = this.mongoClient.getCollection(element.getClass())
					.findOneAndUpdate(
							Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
							new Document().append("$set", element),
							new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
					);
		}
		return updated;
	}

	@Override
	public <T extends Element> T update(String id, Map<String, Object> fieldsAndValues, Class<T> elementSubType) throws DatastoreException {
		T updated = null;
		if (id != null) {
			List<Bson> updates = fieldsAndValues.entrySet().stream().map(fieldAndValue -> {
				Bson update;
				if (FieldNames.METADATA.equals(fieldAndValue.getKey())) {
					update = Updates.addToSet(fieldAndValue.getKey(), fieldAndValue.getValue());
				} else {
					update = Updates.set(fieldAndValue.getKey(), fieldAndValue.getValue());
				}
				return update;
			}).collect(Collectors.toList());
			updates.add(Updates.currentDate(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED));

			updated = this.mongoClient.getCollection(elementSubType)
				.findOneAndUpdate(
						Filters.eq(FieldNames.ID, new ObjectId(id)),
						Updates.combine(updates),
						new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
				);
		}

		return updated;
	}

	@Override
	public <T extends Element> T softDelete(String id, Class<T> elementSubType) throws DatastoreException {
		Map<String, Object> statusFieldAndValue = new HashMap<>();
		statusFieldAndValue.put(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE);
		return update(id, statusFieldAndValue, elementSubType);
	}

	@Override
	public <T extends Element> T  findElementAndupdateMetadata(String id, Set<String> addMetadataIds, Set<String> removeMetadataIds, Class<T> elementSubType) {
		Bson addUpdate = Updates.addEachToSet(FieldNames.METADATA, addMetadataIds.stream().map(metadatumId -> new Document(FieldNames.ID, new ObjectId(metadatumId))).collect(Collectors.toList()));
		List<Bson> removeUpdates = removeMetadataIds.stream().map(metadatumId -> Updates.pullByFilter(Filters.eq(FieldNames.METADATA + "." + FieldNames.ID, new ObjectId(metadatumId)))).collect(Collectors.toList());


		List<Bson> updates = new ArrayList<>();
		updates.add(addUpdate);
		updates.addAll(removeUpdates);

		updates.add(Updates.currentDate(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED));

		return this.mongoClient.getCollection(elementSubType)
				.findOneAndUpdate(
					Filters.eq(FieldNames.ID, new ObjectId(id)),
					Updates.combine(updates),
					new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
				);
	}

	/*@Override
	public <T extends Element> T delete(String id, Class<T> elementSubtype) throws DatastoreException {
		T deletedElement;
		try {
			deletedElement = this.mongoClient.getCollection(elementSubtype).findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

		} catch (IllegalArgumentException e) {
			throw new DatastoreException(e);
		}

		if (deletedElement != null) {
			try {
				this.metadataStore.deleteAll(deletedElement.getId());
			} catch (MetadataStoreException e) {
				throw new DatastoreException("Element " + id + " deletion failed", e);
			}
		}
		return  deletedElement;
	}*/

	/*@Override
	public <T extends Element> List<T> delete(Query<? extends Criterion> getQueryExecutor, Class<T> elementSubtype) throws DatastoreException {
		int deletedCount = 0;
		String subtype = elementSubtype.getSimpleName();

		logger.debug("Delete criteria getQueryExecutor: " + getQueryExecutor);
		
		List<T> toBeDeleted = get(getQueryExecutor, elementSubtype).list();
		
		for (T element: toBeDeleted) {
			if (element instanceof DataElement) {
				try {
					logger.debug("Delete DataElement " + element.getId());
					this.mongoClient.getDataElements().deleteOne(Filters.eq(FieldNames.ID, new ObjectId(element.getId())));
					logger.debug("Delete completed");
					deletedCount++;
				} catch (MongoException e) {
					throw new DatastoreException("Couldn't delete DataElement " + element.getId(), e);
				}
				// TODO: remove DataElement id from Collection
				try {
					this.metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e) {
					logger.error(e.getMessage(), e);
				}
			} else if (element instanceof Collection) {
				try {
					logger.debug("Delete Collection " + element.getId());
					this.mongoClient.getCollections().deleteOne(Filters.eq(FieldNames.ID, element.getId()));
					logger.debug("Delete completed");
					deletedCount++;
				} catch(MongoException e) {
					throw new DatastoreException("Couldn't delete Collection " + element.getId(), e);
				}
				// TODO: remove Collection id from DataElements
				try {
					this.metadataStore.deleteAll(element.getId());
				} catch (MetadataStoreException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		*//*try {
			if (subtype.equals("DataElement")) {
				result = dataElements.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else if (subtype.equals("Collection")) {
				result = collections.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else {
				throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype");
			}
		} catch(MongoException e) {
			throw new DatastoreException("Couldn't delete element(s)", e);
		}*//*

		if (deletedCount == toBeDeleted.size()) {
			logger.info("All " + deletedCount + " elements of type " + subtype + " were successfully deleted");
		} else if (deletedCount < toBeDeleted.size()) {
			logger.info(toBeDeleted.size() + " of " + deletedCount + " elements of type " + subtype + " were successfully deleted");			
		}
		return toBeDeleted;
		
	}*/
	
	@Override
	public DataElement getDataElementByName(String name) throws DatastoreException {
		DataElement dataElement;
		dataElement = this.mongoClient.getDataElements().find(Filters.eq(FieldNames.NAME, name)).limit(1).first();
		/*if (dataElement != null) {
			dataElement.setMetadata(getMetadata(dataElement));
		}*/
		return dataElement;
	}

	@Override
	public <T extends Element> T get(String id, Class<T> elementSubtype) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), elementSubtype).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id [" + id + "]", e);
		}
	}

	@Override
	public <T extends Element> T get(String id, Class<T> elementSubtype,  QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), elementSubtype).options(options).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id [" + id + "]", e);
		}
	}

	/*@Override
	public <T extends Element> T get(String id, Class<T> elementSubtype, MetadataStore metadataStore, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.getQueryExecutor().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), elementSubtype, metadataStore).options(options).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id: [" + id + "]", e);
		}
	}*/

	@Override
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		QueryMongo queryMongo = (QueryMongo) query;
		return QueryExecutorFactory.getQueryExecutor(this, elementSubtype).find(queryMongo);
	}

	/*@Override
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> getQueryExecutor, Class<T> elementSubtype, MetadataStore metadataStore) {
		QueryMongo queryMongo = (QueryMongo) getQueryExecutor;
		return new QueryOptionsBuilderMongo<T>().getQueryExecutor(this, metadataStore, elementSubtype).find(queryMongo);
	}*/

	@Override
	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return QueryExecutorFactory.getQueryExecutor(this, elementSubtype).count(query);
	}
	

	@Override
	public void remove(DataElement dataElement, Collection collection) throws DatastoreException {

	}

	@Override
	public String generateId() {
		return new ObjectId().toString();
	}

	@Override
	public Object generateId(String id) {
		return new ObjectId(id);
	}
}