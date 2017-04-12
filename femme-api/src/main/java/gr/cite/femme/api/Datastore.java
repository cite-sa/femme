package gr.cite.femme.api;

import java.util.List;
import java.util.Map;

import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.query.api.QueryExecutor;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;

public interface Datastore {
	
	public void close();
	
	public String insert(Element element) throws DatastoreException;

	public <T extends Element> List<String> insert(List<T> element) throws DatastoreException;
	
	/*public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException;*/
	
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	public DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException;

	public Element update(Element element) throws DatastoreException;

	public <T extends Element> Element update(String id, Map<String, Object> fieldsAndValues, Class<T> elementSubType) throws DatastoreException;

	public void remove(DataElement dataElement, Collection collection) throws DatastoreException;

	/*public <T extends Element> T delete(String id, Class<T> elementSubtype) throws DatastoreException;

	public <T extends Element> List<T> delete(Query<? extends Criterion> query, Class<T> elementSubtype) throws DatastoreException;*/

	public <T extends Element> T get(String id, Class<T> elementSubtype, MetadataStore metadataStore, QueryOptionsMessenger options) throws DatastoreException;

	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype, MetadataStore metadataStore);

	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	public DataElement getDataElementByName(String id) throws DatastoreException;

	/*public void reIndexAll() throws DatastoreException;*/

	public String generateElementId();
	
}
