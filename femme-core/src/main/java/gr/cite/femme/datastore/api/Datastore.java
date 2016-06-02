package gr.cite.femme.datastore.api;

import java.util.List;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IQuery;
import gr.cite.femme.query.IQueryOptions;

public interface Datastore<R extends ICriteria, S extends IQuery<R>>  {
	<T extends Element> T insert(T element) throws DatastoreException;
	
	<T extends Element> List<T> insert(List<T> elements) throws DatastoreException;
	
	DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	DataElement addToCollection(DataElement dataElement, ICriteria criteria) throws DatastoreException;
	
	Collection addToCollection(List<DataElement> dataElement, ICriteria criteria) throws DatastoreException;
	
	Element update(Element element) throws DatastoreException;

	void remove(Element dataElement, Collection collection) throws DatastoreException;
	
	<T extends Element> void delete(R criteria, Class<T> elementSubtype) throws DatastoreException;
	
	public <T extends Element> IQueryOptions<T> find(S query, Class<T> elementSubtype);
	
	public Collection getCollection(String id);
	
	public DataElement getDataElement(String id);
}
