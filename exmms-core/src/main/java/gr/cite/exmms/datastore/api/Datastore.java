package gr.cite.exmms.datastore.api;

import java.util.List;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.datastore.exceptions.DatastoreException;

public interface Datastore {
	Element insert(DataElement dataElement) throws DatastoreException;
	
	Element update(Element dataElement) throws DatastoreException;

	void delete(Element dataElement) throws DatastoreException;
	
	void add(Element dataElement, Collection collection) throws DatastoreException;
	
	void remove(Element dataElement, Collection collection) throws DatastoreException;

	List<DataElement> listDataElements();
	
	List<Collection> listCollections();
}
