package gr.cite.femme.engine.query.mongodb;

import com.mongodb.client.MongoCollection;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.MetadataQueryExecutor;
import gr.cite.femme.core.query.api.QueryExecutor;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.metadatastore.mongodb.MongoMetadataStore;

public class QueryExecutorFactory<T extends Element> {
	public QueryMongoExecutor.QueryExecutionBuilder<T> query(Datastore datastore, Class<T> elementSubtype) {
		if (datastore instanceof MongoDatastore) {
			return QueryMongoExecutor.builder((MongoDatastore)datastore, elementSubtype);
		} else {
			throw new UnsupportedOperationException(datastore.getClass().getSimpleName() + " datastore not supported yet");
		}

	}

	public MetadataQueryMongoExecutor.QueryExecutionBuilder<T> query(Datastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		if (datastore instanceof MongoDatastore && metadataStore instanceof MongoMetadataStore) {
			return MetadataQueryMongoExecutor.builder((MongoDatastore)datastore, metadataStore, elementSubtype);
		} else {
			throw new UnsupportedOperationException(datastore.getClass().getSimpleName() + " datastore not supported yet");
		}
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
