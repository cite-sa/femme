package gr.cite.femme.query.api;

import java.util.List;

import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.InvalidQueryOperation;
import gr.cite.femme.model.Element;

public interface QueryOptions<T extends Element> {
	
	public QueryOptions<T> limit(Integer limit);
	
	public QueryOptions<T> skip(Integer skip);
	
	public QueryOptions<T> sort(String field, String order) throws InvalidQueryOperation;
	
	public QueryOptions<T> exclude(String ...fields);
	
	public List<T> list() throws DatastoreException;
	
	public T first() throws DatastoreException;
	
	public List<T> xPath(String xPath) throws DatastoreException;
}
