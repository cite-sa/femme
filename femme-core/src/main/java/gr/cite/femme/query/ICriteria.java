package gr.cite.femme.query;

import java.util.Map;

import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.query.IWhere;

public interface ICriteria {
	IWhere where(String elementField) throws InvalidCriteriaQueryOperation;
	
	IWhere and(String elementField) throws InvalidCriteriaQueryOperation ;
	
	ICriteria andOperator(ICriteria... criterias);
	
	ICriteria orOperator(ICriteria... criterias);
	
	ICriteria notOperator(ICriteria criteria);
	
	ICriteria norOperator(ICriteria... criterias);
	
	public ICriteria addToSet(ICriteria newField);
	
	public Map<String, Object> getCriteria();
}