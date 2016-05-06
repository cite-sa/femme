package gr.cite.femme.query.mongodb;

import java.util.Map;

import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IQuery;

public class Query implements IQuery {
	ICriteria criteria;
	
	public Query() {
	}
	
	public Query(ICriteria criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public void addCriteria(ICriteria criteria) {
		this.criteria = criteria; 
	}
	
	@Override
	public Map<String, Object> getQuery() {
		return criteria.getCriteria();
	}
}
