package gr.cite.femme.core.query.api;

import java.util.List;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Element;

public interface QueryExecutor<T extends Element> {
	
	public QueryExecutor<T> options(QueryOptionsMessenger options);

	public <U extends Criterion> QueryExecutor<T> find(Query<U> query);

	public QueryExecutor<T> xPath(String xPath) throws DatastoreException;
	
	public List<T> list() throws DatastoreException;
	
	public T first() throws DatastoreException;
	

	
}
