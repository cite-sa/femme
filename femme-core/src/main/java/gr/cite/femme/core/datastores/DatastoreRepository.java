package gr.cite.femme.core.datastores;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Element;

import java.util.List;
import java.util.Map;

public interface DatastoreRepository<T extends Element> {
	void close();
	
	String insert(T element) throws DatastoreException;
	
	List<String> insert(List<T> elements) throws DatastoreException;
	
	T update(T element) throws DatastoreException;
	
	T update(String id, Map<String, Object> fieldsAndValues);
	
	T replace(T element) throws DatastoreException;
	
	T delete(String id) throws DatastoreException;
	
	T getElementByProperty(String property, Object propertyValue) throws DatastoreException;
	
	List<T> getElementsByProperty(String property, Object propertyValue) throws DatastoreException;
	
	T getElementByProperties(Map<String, String> propertiesAndValues) throws DatastoreException;
	
	List<T> getElementsByProperties(Map<String, String> propertiesAndValues) throws DatastoreException;
	//T find(QueryExecutor<T> queryExecutor) throws MetadataStoreException, DatastoreException;
}
