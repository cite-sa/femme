package gr.cite.femme.datastore.api;

import java.util.List;

import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.CriterionInterface;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryOptions;

public interface Datastore<R extends CriterionInterface, S extends Query<R>>  {
	<T extends Element> String insert(T element) throws DatastoreException;
	
	<T extends Element> List<String> insert(List<T> elements) throws DatastoreException;
	
	DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	DataElement addToCollection(DataElement dataElement, S query) throws DatastoreException;
	
	Collection addToCollection(List<DataElement> dataElement, S query) throws DatastoreException;
	
	<T extends Element> String update(T element) throws DatastoreException;

	void remove(DataElement dataElement, Collection collection) throws DatastoreException;
	
	<T extends Element> void delete(S query, Class<T> elementSubtype) throws DatastoreException;
	
	public <T extends Element> QueryOptions<T> find(S query, Class<T> elementSubtype);
	
	public <T extends Element> long count(S query, Class<T> elementSubtype);
	
	public Collection getCollection(String id) throws DatastoreException;
	
	public DataElement getDataElement(String id) throws DatastoreException;
	
	public DataElement getDataElementByName(String id) throws DatastoreException;
}
