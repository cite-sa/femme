package gr.cite.femme.query.api;

import java.util.List;

import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.InvalidQueryOperation;
import gr.cite.femme.model.Element;

public interface QueryOptions<T extends Element> {
	
	/*public QueryOptions<T> limit(Integer limit);
	
	public QueryOptions<T> skip(Integer skip);
	
	public QueryOptions<T> asc(String field);
	
	public QueryOptions<T> desc(String field);
	
	public QueryOptions<T> include(String ...fields);
	
	public QueryOptions<T> exclude(String ...fields);*/
	
	public QueryOptions<T> options(QueryOptionsFields options);

	public <U extends Criterion> QueryOptions<T> find(Query<U> query);

	public QueryOptions<T> xPath(String xPath) throws DatastoreException;
	
	public List<T> list() throws DatastoreException;
	
	public T first() throws DatastoreException;
	

	
}
