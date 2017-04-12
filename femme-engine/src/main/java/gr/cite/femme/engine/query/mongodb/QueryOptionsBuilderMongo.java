package gr.cite.femme.engine.query.mongodb;

import com.mongodb.client.MongoCollection;

import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.core.model.Element;

public class QueryOptionsBuilderMongo<T extends Element> {
	public QueryMongoExecutor<T> query(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		return new QueryMongoExecutor<>(datastore, metadataStore, elementSubtype);
	}

	public long count(MongoDatastore datastore, Class<T> elementSubtype) {
		MongoCollection<T> collection = datastore.getCollection(elementSubtype);
		return collection.count();
	}

	public long count(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
		MongoCollection<T> collection = datastore.getCollection(elementSubtype);
		return query == null ? collection.count() : collection.count(query.build());
	}
}
