package gr.cite.femme.datastore.mongodb.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.mongodb.MongoDatastoreClient;
import gr.cite.femme.datastore.mongodb.bson.CollectionBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.DataElementBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.ElementBson;
import gr.cite.femme.datastore.mongodb.bson.ElementBsonBuilder;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.query.criteria.Where;

public class MongoCriteriaQuery implements CriteriaQuery<Element> {
	private MongoCollection<Element> elementCollection;
	
	
	
	public MongoCriteriaQuery(MongoDatastoreClient mongoClient) {
	}



	@Override
	public Where<Element> whereBuilder() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public <S> Where<S> expressionFactory() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Element find(String id) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<Element> find(Element t) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<Element> find() {
		// TODO Auto-generated method stub
		return null;
	}
}
