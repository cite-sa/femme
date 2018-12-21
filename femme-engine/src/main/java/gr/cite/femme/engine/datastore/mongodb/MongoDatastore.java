package gr.cite.femme.engine.datastore.mongodb;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gr.cite.femme.core.datastores.DatastoreRepositoryProvider;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.engine.datastore.mongodb.repositories.CollectionMongoDatastoreRepository;
import gr.cite.femme.engine.datastore.mongodb.repositories.DataElementMongoDatastoreRepository;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import gr.cite.femme.engine.query.execution.mongodb.QueryExecutorFactory;
import gr.cite.femme.core.query.execution.QueryExecutor;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.Filters;

import gr.cite.femme.core.datastores.Datastore;
/*import gr.cite.femme.datastore.mongodb.cache.MongoXPathCacheManager;*/
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;

import javax.inject.Inject;

public class MongoDatastore implements Datastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);
	
	private DatastoreRepositoryProvider datastoreRepositoryProvider;

	@Inject
	public MongoDatastore(DatastoreRepositoryProvider datastoreRepositoryProvider) {
		this.datastoreRepositoryProvider = datastoreRepositoryProvider;
	}
	
	@Override
	public DatastoreRepositoryProvider getDatastoreRepositoryProvider() {
		return datastoreRepositoryProvider;
	}
	
	@Override
	public <T extends Element> String insert(T element) throws DatastoreException {
		if (element.getSystemicMetadata() == null) element.setSystemicMetadata(new SystemicMetadata());
		
		Instant now = Instant.now();
		element.getSystemicMetadata().setCreated(now);
		element.getSystemicMetadata().setModified(now);
		element.getSystemicMetadata().setStatus(Status.ACTIVE);
		
		return this.datastoreRepositoryProvider.get(element).insert(element);
	}

	@Override
	public <T extends Element> List<String> insert(List<T> elements, Class<T> elementSubtype) throws DatastoreException {
		if (elements == null || elements.size() == 0) throw new IllegalArgumentException("List must contain elements");
		
		elements.forEach(element -> {
			Instant now = Instant.now();
			element.getSystemicMetadata().setCreated(now);
			element.getSystemicMetadata().setModified(now);
		});
			
		return this.datastoreRepositoryProvider.get(elementSubtype).insert(elements);
	}

	@Override
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException, MetadataStoreException {
		return addToCollection(dataElement, QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, generateId(collectionId)).end()));
	}

	@Override
	public DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException, MetadataStoreException {
		QueryMongo mongoQuery = (QueryMongo) query;
		logger.debug("addToCollection criteria getQueryExecutor: " + mongoQuery.build());
		//find(getQueryExecutor, Collection.class).first()
		Collection collection = QueryExecutorFactory.getQueryExecutor(this.datastoreRepositoryProvider, Collection.class).find(mongoQuery).first();
		//Collection collection = this.datastoreRepositoryProvider.get(dataElement).find(mongoQuery.build()).limit(1).first();

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

	@Override
	public <T extends Element> T update(T element) throws DatastoreException {
		if (element.getId() != null) {
			if (element.getSystemicMetadata() != null) {
				element.getSystemicMetadata().setModified(Instant.now());
			}
			
			return this.datastoreRepositoryProvider.get(element).update(element);
		}
		return null;
	}

	@Override
	public <T extends Element> T update(String id, Map<String, Object> fieldsAndValues, Class<T> elementSubType) {
		if (id == null || "".equals(id)) throw new IllegalArgumentException("ID must have value");
		
		return this.datastoreRepositoryProvider.get(elementSubType).update(id, fieldsAndValues);
	}

	@Override
	public <T extends Element> T softDelete(String id, Class<T> elementSubType) {
		Map<String, Object> statusFieldAndValue = new HashMap<>();
		statusFieldAndValue.put(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE);
		return update(id, statusFieldAndValue, elementSubType);
	}

	@Override
	public <T extends Element> T delete(Element element, Class<T> elementSubtype) throws DatastoreException {
		return this.datastoreRepositoryProvider.get(elementSubtype).delete(element.getId());
	}

	/*@Override
	public <T extends Element> T findElementAndUpdateMetadata(String id, Set<String> addMetadataIds, Set<String> removeMetadataIds, Class<T> elementSubType) {
		Bson addUpdate = Updates.addEachToSet(FieldNames.METADATA, addMetadataIds.stream().map(metadatumId -> new Document(FieldNames.ID, generateId(metadatumId))).collect(Collectors.toList()));
		List<Bson> removeUpdates = removeMetadataIds.stream().map(metadatumId -> Updates.pullByFilter(Filters.eq(FieldNames.METADATA + "." + FieldNames.ID, generateId(metadatumId)))).collect(Collectors.toList());

		List<Bson> updates = new ArrayList<>();
		updates.add(addUpdate);
		updates.addAll(removeUpdates);

		updates.add(Updates.currentDate(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED));

		return this.datastoreRepository.getCollection(elementSubType).findOneAndUpdate(
				Filters.eq(FieldNames.ID, generateId(id)),
				Updates.combine(updates),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
	}*/
	
	@Override
	public <T extends Element> T getElementByName(String name, Class<T> elementSubtype) throws DatastoreException {
		try {
			return this.datastoreRepositoryProvider.get(elementSubtype).getElementByProperty(FieldNames.NAME, name);
		} catch (Exception e) {
			throw new DatastoreException("Error on DataElement [" + name + "] retrieval", e);
		}
	}

	@Override
	public <T extends Element> T get(String id, Class<T> elementSubtype) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, generateId(id)).end()), elementSubtype).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id [" + id + "]", e);
		}
	}

	@Override
	public <T extends Element> T get(String id, Class<T> elementSubtype, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, generateId(id)).end()), elementSubtype).options(options).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException(elementSubtype.getSimpleName() + " retrieval: invalid id [" + id + "]", e);
		}
	}
	
	@Override
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return QueryExecutorFactory.getQueryExecutor(this.datastoreRepositoryProvider, elementSubtype).find(query);
	}
	
	@Override
	public List<DataElement> getDataElementsByCollection(String collectionId) throws DatastoreException {
		try {
			return getDataElementDatastoreRepository().find(Filters.all(FieldNames.COLLECTIONS, collectionId));
		} catch (Exception e) {
			throw new DatastoreException("Retrieval of collection's [" + collectionId + "] data elements failed", e);
		}
	}
	
	@Override
	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return QueryExecutorFactory.getQueryExecutor(this.datastoreRepositoryProvider, elementSubtype).count(query);
	}

	@Override
	public Collection getCollectionByNameAndEndpoint(String name, String endpoint) throws DatastoreException {
		Map<String, String> propertiesAndValues = new HashMap<>();
		
		propertiesAndValues.put(FieldNames.NAME, name);
		propertiesAndValues.put(FieldNames.ENDPOINT, endpoint);
		
		return this.getCollectionDatastoreRepository().getElementByProperties(propertiesAndValues);
	}
	
	@Override
	public DataElement getDataElementByNameEndpointAndCollections(String name, String endpoint, List<Collection> collections) {
		if (name == null || endpoint == null || collections == null) return null;
		
		List<String> collectionsIds = collections.stream().map(Element::getId).collect(Collectors.toList());
		
		Bson filterByName = Filters.eq(FieldNames.NAME, name);
		Bson filterByEndpoint = Filters.eq(FieldNames.ENDPOINT, endpoint);
		Bson filterByCollections = Filters.all(FieldNames.COLLECTIONS, collectionsIds);
		
		return this.getDataElementDatastoreRepository().get(Filters.and(filterByName, filterByEndpoint, filterByCollections));
	}
	
	private CollectionMongoDatastoreRepository getCollectionDatastoreRepository() {
		return (CollectionMongoDatastoreRepository) this.datastoreRepositoryProvider.get(Collection.class);
	}
	
	private DataElementMongoDatastoreRepository getDataElementDatastoreRepository() {
		return (DataElementMongoDatastoreRepository) this.datastoreRepositoryProvider.get(DataElement.class);
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