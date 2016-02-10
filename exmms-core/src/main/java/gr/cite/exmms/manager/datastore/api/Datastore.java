package gr.cite.exmms.manager.datastore.api;

import java.util.List;

import gr.cite.exmms.manager.core.Collection;
import gr.cite.exmms.manager.core.DataElement;
import gr.cite.exmms.manager.datastore.exceptions.DatastoreException;

public interface Datastore {
	DataElement insert(DataElement dataElement) throws DatastoreException;
	
	DataElement update(DataElement dataElement) throws DatastoreException;

	void remove(DataElement dataElement) throws DatastoreException;
	
	void add(DataElement dataElement, Collection collection) throws DatastoreException;

	List<DataElement> listDataElements();
	
	List<Collection> listCollections();
}
