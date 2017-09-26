package gr.cite.femme.core.datastores;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.core.dto.QueryOptionsMessenger;

public interface Datastore {
	
	public void close();

	public String insert(Element element) throws DatastoreException;

	public List<String> insert(List<? extends Element> element) throws DatastoreException;
	
	/*public <T extends Element> List<String> insert(List<T> elements) throws DatastoreException;*/
	
	public DataElement addToCollection(DataElement dataElement, String collectionId) throws DatastoreException;
	
	public DataElement addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException;

	public <T extends Element> T update(Element element) throws DatastoreException;

	public <T extends Element> T  update(String id, Map<String, Object> fieldsAndValues, Class<T> elementSubType) throws DatastoreException;

	public <T extends Element> T softDelete(String id, Class<T> elementSubType) throws DatastoreException;

	public <T extends Element> T delete(String id, Class<T> elementSubType) throws DatastoreException;

	public <T extends Element> T findElementAndUpdateMetadata(String id, Set<String> addMetadataIds, Set<String> removeMetadataIds, Class<T> elementSubType);

	public void remove(DataElement dataElement, Collection collection) throws DatastoreException;

	/*public <T extends Element> T delete(String id, Class<T> elementSubtype) throws DatastoreException;

	public <T extends Element> List<T> delete(Query<? extends Criterion> query, Class<T> elementSubtype) throws DatastoreException;*/

	public <T extends Element> T get(String id, Class<T> elementSubtype) throws DatastoreException, MetadataStoreException;

	public <T extends Element> T get(String id, Class<T> elementSubtype, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException;

	//public <T extends Element> T get(String id, Class<T> elementSubtype, MetadataStore metadataStore, QueryOptionsMessenger options) throws DatastoreException, MetadataStoreException;

	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype);

	//public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype, MetadataStore metadataStore);

	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype);
	
	public DataElement getDataElementByName(String id) throws DatastoreException;

	/*public void reIndexAll() throws DatastoreException;*/

	public String generateId();

	public Object generateId(String id);
	
}
