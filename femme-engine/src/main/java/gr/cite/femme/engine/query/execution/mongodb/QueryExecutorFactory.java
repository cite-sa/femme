package gr.cite.femme.engine.query.execution.mongodb;

import gr.cite.femme.core.datastores.Datastore;
import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.execution.MetadataQueryExecutorBuilder;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.metadatastore.mongodb.MongoMetadataStore;

public class QueryExecutorFactory {
	public static <T extends Element> QueryExecutor<T> getQueryExecutor(Datastore datastore, Class<T> elementSubtype) {
		if (datastore instanceof MongoDatastore) {
			return new QueryMongoExecutor<>((MongoDatastore)datastore, elementSubtype);
		} else {
			throw new UnsupportedOperationException(datastore.getClass().getSimpleName() + " datastore not supported yet");
		}
	}

	public static <T extends Element> MetadataQueryExecutorBuilder<T> getQueryExecutor(Datastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		if (datastore instanceof MongoDatastore && metadataStore instanceof MongoMetadataStore) {
			return new MetadataQueryMongoExecutorBuilder<>((MongoDatastore)datastore, metadataStore, elementSubtype);
		} else {
			throw new UnsupportedOperationException(datastore.getClass().getSimpleName() + " datastore not supported yet");
		}
	}
}
