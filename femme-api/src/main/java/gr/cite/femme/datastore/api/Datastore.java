package gr.cite.femme.datastore.api;

import java.util.List;

import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

public interface Datastore {
	
	public void close();
	
	public String insert(Element element) throws DatastoreException;

	public <T extends Element> List<String> insert(List<T> element) throws DatastoreException;
	
	/*public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException;*/
	
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	public DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException;

	public Element update(Element element) throws DatastoreException;

	public void remove(DataElement dataElement, Collection collection) throws DatastoreException;

	/*public <T extends Element> T delete(String id, Class<T> elementSubtype) throws DatastoreException;

	public <T extends Element> List<T> delete(Query<? extends Criterion> query, Class<T> elementSubtype) throws DatastoreException;*/

	public <T extends Element> T get(String id, Class<T> elementSubtype, MetadataStore metadataStore) throws DatastoreException;

	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype, MetadataStore metadataStore);

	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	public DataElement getDataElementByName(String id) throws DatastoreException;

	/*public void reIndexAll() throws DatastoreException;*/

	public String generateElementId();
	
}
