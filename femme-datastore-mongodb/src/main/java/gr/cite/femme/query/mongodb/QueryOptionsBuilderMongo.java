package gr.cite.femme.query.mongodb;

import com.mongodb.client.MongoCollection;

import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;

public class QueryOptionsBuilderMongo<T extends Element> {
	
	public QueryMongoExecutor<T> query(MongoDatastore datastore, Class<T> elementSubtype) {
		
		return new QueryMongoExecutor<T>(datastore, elementSubtype);

	}
	
	public long count(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
		MongoCollection<?> collection = null;
		if (elementSubtype == DataElement.class) {
			collection = datastore.getDataElements();
		} else if (elementSubtype == Collection.class) {
			collection = datastore.getCollections();
		}
		
		return query == null ? collection.count() : collection.count(query.build());
	}
}
