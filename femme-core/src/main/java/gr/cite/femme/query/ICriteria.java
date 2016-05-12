package gr.cite.femme.query;

import java.util.List;
import java.util.Map;

import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.query.IWhere;

public interface ICriteria {
	<T extends ICriteria> IWhere<T> where(String elementField) throws InvalidCriteriaQueryOperation;
	
	<T extends ICriteria> IWhere<T> and(String elementField) throws InvalidCriteriaQueryOperation ;
	
	ICriteria andOperator(ICriteria... criterias);
	
	ICriteria orOperator(ICriteria... criterias);
	
	ICriteria notOperator(ICriteria criteria);
	
	ICriteria norOperator(ICriteria... criterias);
	
	public ICriteria addToSet(ICriteria newField);
	
	public ICriteria inCollection(List<String> collectionIds);
	
	public ICriteria inCollection(ICriteria collectionCriteria);
	
	public ICriteria hasDataElements(List<String> dataElementIds);
	
	public ICriteria hasDataElements(ICriteria dataElementCriteria);
	
	public Map<String, Object> getCriteria();
}