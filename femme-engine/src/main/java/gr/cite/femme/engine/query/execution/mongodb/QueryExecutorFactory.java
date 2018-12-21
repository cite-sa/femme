package gr.cite.femme.engine.query.execution.mongodb;

import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.execution.MetadataQueryExecutorBuilder;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.core.datastores.DatastoreRepositoryProvider;

public class QueryExecutorFactory {
	public static <T extends Element> QueryExecutor<T> getQueryExecutor(DatastoreRepositoryProvider datastoreRepositoryProvider, Class<T> elementSubtype) {
		return new QueryMongoExecutor<>(datastoreRepositoryProvider, elementSubtype);
	}

	public static <T extends Element> MetadataQueryExecutorBuilder<T> getQueryExecutor(DatastoreRepositoryProvider datastoreRepositoryProvider, MetadataStore metadataStore, Class<T> elementSubtype) {
		return new MetadataQueryMongoExecutorBuilder<>(datastoreRepositoryProvider, metadataStore, elementSubtype);
	}
}
