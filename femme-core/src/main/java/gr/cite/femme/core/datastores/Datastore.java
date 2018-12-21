package gr.cite.femme.core.datastores;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.core.dto.QueryOptionsMessenger;

public interface Datastore {
	DatastoreRepositoryProvider getDatastoreRepositoryProvider();
	
	<T extends Element> String insert(T element) throws DatastoreException;

	<T extends Element> List<String> insert(List<T> elements, Class<T> elementSubtype) throws DatastoreException;
	
	DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException, MetadataStoreException;
	
	DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException, MetadataStoreException;

	<T extends Element> T update(T element) throws DatastoreException;

	<T extends Element> T  update(String id, Map<String, Object> fieldsAndValues, Class<T> elementSubType) throws DatastoreException;

	<T extends Element> T softDelete(String id, Class<T> elementSubType) throws DatastoreException;
	
	<T extends Element> T delete(Element element, Class<T> elementSubtype) throws DatastoreException;

	//<T extends Element> T findElementAndUpdateMetadata(String id, Set<String> addMetadataIds, Set<String> removeMetadataIds, Class<T> elementSubType);

	//<T extends Element> List<T> delete(Query<? extends Criterion> query, Class<T> elementSubtype) throws DatastoreException;

	<T extends Element> T get(String id, Class<T> elementSubtype) throws DatastoreException, MetadataStoreException;

	<T extends Element> T get(String id, Class<T> elementSubtype, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException;

	<T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype);

	<T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	<T extends Element> T getElementByName(String name, Class<T> elementSubtype) throws DatastoreException;
	
	List<DataElement> getDataElementsByCollection(String collectionId) throws DatastoreException;

	Collection getCollectionByNameAndEndpoint(String name, String endpoint) throws DatastoreException;
	
	DataElement getDataElementByNameEndpointAndCollections(String name, String endpoint, List<Collection> collectionIds);

	String generateId();

	Object generateId(String id);
	
}
