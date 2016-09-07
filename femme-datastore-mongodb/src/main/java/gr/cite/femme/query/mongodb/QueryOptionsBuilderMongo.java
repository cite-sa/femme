package gr.cite.femme.query.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;

public class QueryOptionsBuilderMongo<T extends Element> {
	
	public QueryOptionsMongo<T> find(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
		
		return new QueryOptionsMongo<T>(query, datastore, elementSubtype);

	}
	
	public long count(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
		MongoCollection<T> collection = null;
		if (elementSubtype == DataElement.class) {
			collection = (MongoCollection<T>) datastore.getDataElements();
		} else if (elementSubtype == Collection.class) {
			collection = (MongoCollection<T>) datastore.getCollections();
		}
		
		return query == null ? collection.count() : collection.count(query.build());
	}
}
