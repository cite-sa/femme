package gr.cite.femme.core.query.api;

import java.util.List;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;

public interface QueryExecutor<T extends Element> {
	
	public QueryExecutor<T> options(QueryOptionsMessenger options);

	public QueryExecutor<T> find(Query<? extends Criterion> query);

	//public QueryExecutor<T> xPath(String xPath) throws DatastoreException, MetadataStoreException;
	
	public List<T> list() throws DatastoreException, MetadataStoreException;
	
	public T first() throws DatastoreException, MetadataStoreException;

}
