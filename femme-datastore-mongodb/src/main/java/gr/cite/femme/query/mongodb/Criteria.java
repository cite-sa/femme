package gr.cite.femme.query.mongodb;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IWhere;

public class Criteria implements ICriteria {
	private Where where;
	
	private Map<String, Object> criteria = new LinkedHashMap<String, Object>();
	private String key = null;
	
	private boolean or;
	Map<String, Object> orMap;
	private List<Map<String, Object>> orList;
	
	private boolean collectionsResolved = true;
	private boolean dataElementsResolved = true;
	
	
	public Criteria() {
		where = new Where(this);
	}
	
	public Criteria(String field) {
		where = new Where(this);
		key = field;
	}
	
	public boolean isCollectionsResolved() {
		return collectionsResolved;
	}

	public void resolveCollections() {
		this.collectionsResolved = true;
	}
	
	public boolean isDataElementsResolved() {
		return dataElementsResolved;
	}

	public void resolveDataElements() {
		this.dataElementsResolved = true;
	}
	
	public static Criteria query() {
		return new Criteria();
	}
	@Override
	public Where where(String elementField) throws InvalidCriteriaQueryOperation {
		if (!criteria.isEmpty()) {
			throw new InvalidCriteriaQueryOperation("Where is not allowed");
		}
		key = elementField;
		return where;
	}
	
	@Override
	public Where and(String elementField) throws InvalidCriteriaQueryOperation {
		if (criteria.containsKey(elementField)) {
			throw new InvalidCriteriaQueryOperation("And is not allowed on same field");
		}
		key = elementField;
		return where;
	}

	@Override
	public ICriteria andOperator(ICriteria ... criterias) {
		criteria.put("$and", arrayToList(criterias));
		return this;
	}
	
	@Override
	public ICriteria orOperator(ICriteria... criterias) {
		criteria.put("$or", arrayToList(criterias));
		return this;
	}
	
	@Override
	public ICriteria notOperator(ICriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICriteria norOperator(ICriteria... criterias) {
		criteria.put("$nor", arrayToList(criterias));
		return this;
	}
	
	@Override
	public ICriteria addToSet(ICriteria newField) {
		criteria.put("$addToSet", newField);
		return this;
	}
	
	@Override
	public ICriteria inCollection(List<String> collectionIds) {
		Map<String, Object> collections = new LinkedHashMap<>();
		collections.put("$in", 
				collectionIds.stream().map(new Function<String, Document>() {

					@Override
					public Document apply(String collectionId) {
						return new Document().append(FieldNames.ID, new ObjectId(collectionId));
					}
				}).collect(Collectors.toList())
				);
		criteria.put("collections", collections);
		
		collectionsResolved = true;
		return this;
	}
	
	@Override
	public ICriteria inCollection(ICriteria collectionCriteria) {
		Map<String, Object> collections = new LinkedHashMap<>();
		collections.put("$in", collectionCriteria);
		criteria.put("collections", collections);
		
		collectionsResolved = false;
		return this;
	}
	
	@Override
	public ICriteria hasDataElements(List<String> dataElementIds) {
		Map<String, Object> dataElements = new LinkedHashMap<>();
		dataElements.put("$in", 
				dataElementIds.stream().map(new Function<String, Document>() {

					@Override
					public Document apply(String collectionId) {
						return new Document().append(FieldNames.ID, new ObjectId(collectionId));
					}
				}).collect(Collectors.toList()));
		criteria.put("dataElements", dataElements);
		
		dataElementsResolved = true;
		return this;
	}
	
	@Override
	public ICriteria hasDataElements(ICriteria dataElementCriteria) {
		Map<String, Object> dataElements = new LinkedHashMap<>();
		dataElements.put("$in", dataElementCriteria);
		criteria.put("dataElements", dataElements);
		
		dataElementsResolved = false;
		return this;
	}
	
	/*@Override
	public ICriteria hasDataElements(ICriteria dataElementCriteria) {
		Map<String, Object> dataElements = new LinkedHashMap<>();
		dataElements.put("$in", dataElementCriteria);
		criteria.put("dataElements", dataElements);
		
		dataElementsResolved = false;
		return this;
	}*/
	
	@Override
	public Map<String, Object> getCriteria() {
		return criteria;
	}
	
	@Override
	public String toString() {
		return criteria.toString();
	}

	public <T> void setValue(T value) {
		if (key != null) {
			if (or) {
				Map<String, Object> orMap = new LinkedHashMap<>();
				orMap.put(key, value);
				orList.add(orMap);
				/*key = null;*/
				orMap = null;
			} else {
				criteria.put(key, value);
				/*key = null;*/
			}
		}
	}
	
	private List<Map<String, Object>> arrayToList(ICriteria[] criterias) {
		return Arrays.stream(criterias).map(new Function<ICriteria, Map<String, Object>>() {
			@Override
			public Map<String, Object> apply(ICriteria iCriteria) {
				Criteria criteria = (Criteria) iCriteria;
				return criteria.getCriteria();
			}
		}).collect(Collectors.toList());
	}

}
