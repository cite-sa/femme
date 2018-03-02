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
import com.mongodb.client.model.Projections;
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

	public MongoDatastore() {
		this.mongoClient = new MongoDatastoreClient();
	}

	public MongoDatastore(String host, int port, String name) {
		this.mongoClient = new MongoDatastoreClient(host, port, name);
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
					logger.info("Collection [" + element.getEndpoint() + "] already exists.");
				} else {
					throw new DatastoreException("Collection [" + element.getName() + "] insertion failed", e);
				}
			}
		} else if (element instanceof DataElement) {
			try {
				this.mongoClient.getDataElements().insertOne((DataElement) element);
			} catch (MongoException e) {
				throw new DatastoreException("DataElement [" + element.getName() + "] insertion failed", e);
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
	public <T extends Element> T update(T element) throws DatastoreException {
		T updated = null;

		if (element.getId() != null) {
			if (element.getSystemicMetadata() != null) {
				element.getSystemicMetadata().setModified(Instant.now());
			}

			try {
				updated = (T) this.mongoClient.getCollection(element.getClass()).findOneAndUpdate(
						Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
						new Document().append("$set", element),
						new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
			} catch (Exception e) {
				throw new DatastoreException("Error on " + element.getClass() + " [" + element.getId() + "] update", e);
			}
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
	public <T extends Element> T delete(String id, Class<T> elementSubtype) throws DatastoreException {
		T deletedElement;
		try {
			deletedElement = this.mongoClient.getCollection(elementSubtype).findOneAndDelete(Filters.eq(FieldNames.ID, new ObjectId(id)));
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(e);
		}

		return deletedElement;
	}

	@Override
	public <T extends Element> T findElementAndUpdateMetadata(String id, Set<String> addMetadataIds, Set<String> removeMetadataIds, Class<T> elementSubType) {
		Bson addUpdate = Updates.addEachToSet(FieldNames.METADATA, addMetadataIds.stream().map(metadatumId -> new Document(FieldNames.ID, new ObjectId(metadatumId))).collect(Collectors.toList()));
		List<Bson> removeUpdates = removeMetadataIds.stream().map(metadatumId -> Updates.pullByFilter(Filters.eq(FieldNames.METADATA + "." + FieldNames.ID, new ObjectId(metadatumId)))).collect(Collectors.toList());

		List<Bson> updates = new ArrayList<>();
		updates.add(addUpdate);
		updates.addAll(removeUpdates);

		updates.add(Updates.currentDate(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED));

		return this.mongoClient.getCollection(elementSubType).findOneAndUpdate(
				Filters.eq(FieldNames.ID, new ObjectId(id)),
				Updates.combine(updates),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
	}

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
					this.mongoClient.getElements().deleteOne(Filters.eq(FieldNames.ID, new ObjectId(element.getId())));
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
		try {
			return this.mongoClient.getDataElements().find(Filters.eq(FieldNames.NAME, name)).limit(1).first();
		} catch (Exception e) {
			throw new DatastoreException("Error on DataElement [" + name + "] retrieval", e);
		}
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
	public <T extends Element> T get(String id, Class<T> elementSubtype, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), elementSubtype).options(options).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id [" + id + "]", e);
		}
	}
	
	@Override
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		QueryMongo queryMongo = (QueryMongo) query;
		return QueryExecutorFactory.getQueryExecutor(this, elementSubtype).find(queryMongo);
	}
	
	@Override
	public List<DataElement> getDataElementsByCollection(String collectionId) throws DatastoreException {
		List<DataElement> dataElements = new ArrayList<>();
		
		try {
			this.mongoClient.getDataElements().find(Filters.all(FieldNames.COLLECTIONS, collectionId))
					.projection(Projections.include(FieldNames.ID)).into(dataElements);
		} catch (Exception e) {
			throw new DatastoreException("Retrieval of collection's [" + collectionId + "] data elements failed", e);
		}
		
		return dataElements;
	}
	
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
	
	
	@Override
	public Collection getCollectionByNameAndEndpoint(String name, String endpoint) {
		Bson filterByName = Filters.eq(FieldNames.NAME, name);
		Bson filterByEndpoint = Filters.eq(FieldNames.ENDPOINT, endpoint);
		
		return this.mongoClient.getCollection(Collection.class).find(Filters.and(filterByName, filterByEndpoint)).limit(1).first();
	}
	
	@Override
	public DataElement getDataElementByNameEndpointAndCollections(String name, String endpoint, List<Collection> collections) {
		if (name == null || endpoint == null || collections == null) return null;
		
		List<String> collectionsIds = collections.stream().map(Element::getId).collect(Collectors.toList());
		
		Bson filterByName = Filters.eq(FieldNames.NAME, name);
		Bson filterByEndpoint = Filters.eq(FieldNames.ENDPOINT, endpoint);
		Bson filterByCollections = Filters.all(FieldNames.COLLECTIONS, collectionsIds);
		
		return this.mongoClient.getCollection(DataElement.class).find(Filters.and(filterByName, filterByEndpoint, filterByCollections)).limit(1).first();
	}
	
}