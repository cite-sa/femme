package gr.cite.femme.query;

import java.util.Map;


public interface IQuery<T extends ICriteria> {
	public void addCriteria(T criteria);
	
	public Map<String, Object> getQuery();
}
