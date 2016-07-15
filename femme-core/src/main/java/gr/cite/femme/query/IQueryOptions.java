package gr.cite.femme.query;

import java.util.List;

import gr.cite.femme.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;

public interface IQueryOptions<T extends Element> {
	public IQueryOptions<T> count();

	public IQueryOptions<T> limit(int limit);
	
	public IQueryOptions<T> skip(int skip);
	
	public IQueryOptions<T> sort(String field, String order) throws InvalidCriteriaQueryOperation;
	
	public List<T> list();
	
	public List<T> xPath(String xPath);
}
