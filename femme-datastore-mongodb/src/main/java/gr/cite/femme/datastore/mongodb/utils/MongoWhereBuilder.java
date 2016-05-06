package gr.cite.femme.datastore.mongodb.utils;

import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.Where;
import gr.cite.femme.query.criteria.WhereBuilder;

public class MongoWhereBuilder implements WhereBuilder<Element> {
	MongoWhere where;
	
	public MongoWhereBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	public MongoWhereBuilder(MongoWhere where) {
		this.where = where;
	}

	@Override
	public MongoWhere or() {
		// TODO Auto-generated method stub
		return where;
	}

	@Override
	public MongoWhere and() {
		// TODO Auto-generated method stub
		return where;
	}

	@Override
	public CriteriaQuery<Element> build() {
		// TODO Auto-generated method stub
		return null;
	}

}
