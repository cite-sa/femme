package gr.cite.femme.datastore.api;

import java.util.List;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.exceptions.DatastoreException;

public interface Datastore {
	<T extends Element> T insert(T element) throws DatastoreException;
	
	<T extends Element> List<T> insert(List<T> elements) throws DatastoreException;
	
	Element update(Element element) throws DatastoreException;

	void remove(Element element) throws DatastoreException;
	
	void add(Element dataElement, Collection collection) throws DatastoreException;
	
	void delete(Element dataElement, Collection collection) throws DatastoreException;
	
	List<Element> listElements() throws DatastoreException;
	
	List<DataElement> listDataElements() throws DatastoreException;
	
	List<Collection> listCollections() throws DatastoreException;
}
