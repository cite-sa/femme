package gr.cite.femme.datastore.api;

import java.util.List;

import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

public interface Datastore<R extends Criterion, S extends Query<R>>  {
	
	public void close();
	
	public <T extends Element> String insert(T element) throws DatastoreException;
	
	public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException;
	
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	public DataElement addToCollection(DataElement dataElement, S query) throws DatastoreException;
	
	public Collection addToCollection(List<DataElement> dataElement, S query) throws DatastoreException;
	
	public <T extends Element> String update(T element) throws DatastoreException;

	public void remove(DataElement dataElement, Collection collection) throws DatastoreException;
	
	public <T extends Element> void delete(S query, Class<T> elementSubtype) throws DatastoreException;
	
	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	public Collection getCollection(String id) throws DatastoreException;
	
	public DataElement getDataElement(String id) throws DatastoreException;
	
	public DataElement getDataElementByName(String id) throws DatastoreException;
	
}
