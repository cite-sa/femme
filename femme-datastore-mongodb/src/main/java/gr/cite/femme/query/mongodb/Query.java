package gr.cite.femme.query.mongodb;

import java.util.Map;

import gr.cite.femme.query.IQuery;

public class Query implements IQuery<Criteria> {
	private Criteria criteria;
	
	private boolean collectionsResolved;
	
	private boolean dataElementsResolved;
	
	public Query() {
		
	}
	
	public Query(Criteria criteria) {
		this.criteria = (Criteria) criteria;
		this.collectionsResolved = this.criteria.isCollectionsResolved();
		this.dataElementsResolved = this.criteria.isDataElementsResolved();
	}
	
	public boolean isCollectionsResolved() {
		return collectionsResolved;
	}

	public void resolveCollections() {
		criteria.resolveCollections();
		this.collectionsResolved = true;
	}
	
	public boolean isDataElementsResolved() {
		return dataElementsResolved;
	}

	public void resolveDataElements() {
		criteria.resolveDataElements();
		this.dataElementsResolved = true;
	}
	
	@Override
	public void addCriteria(Criteria criteria) {
		this.criteria = (Criteria) criteria;
		this.collectionsResolved = this.criteria.isCollectionsResolved();
		this.dataElementsResolved = this.criteria.isDataElementsResolved();
	}
	
	@Override
	public Map<String, Object> getQuery() {
		return criteria.getCriteria();
	}
}
