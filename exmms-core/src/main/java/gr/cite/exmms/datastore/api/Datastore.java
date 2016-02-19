package gr.cite.exmms.datastore.api;

import java.util.List;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.datastore.exceptions.DatastoreException;

public interface Datastore {
	<T extends Element> T insert(T element) throws DatastoreException;
	
	<T extends Element> List<T> insert(List<T> dataElements) throws DatastoreException;
	
	Element update(Element dataElement) throws DatastoreException;

	void remove(Element dataElement) throws DatastoreException;
	
	void add(Element dataElement, Collection collection) throws DatastoreException;
	
	void delete(Element dataElement, Collection collection) throws DatastoreException;
	
	List<DataElement> listDataElements() throws DatastoreException;
	
	List<Collection> listCollections() throws DatastoreException;
}
