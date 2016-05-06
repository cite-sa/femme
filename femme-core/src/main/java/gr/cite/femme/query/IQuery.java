package gr.cite.femme.query;

import java.util.Map;


public interface IQuery {
	public void addCriteria(ICriteria criteria);
	
	public Map<String, Object> getQuery();
}
