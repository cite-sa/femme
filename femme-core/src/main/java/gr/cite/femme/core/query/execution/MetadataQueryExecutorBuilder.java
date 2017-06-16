package gr.cite.femme.core.query.execution;

import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;

public interface MetadataQueryExecutorBuilder<T extends Element> {
	FindQueryExecutorBuilder<T> find(Query<? extends Criterion> query);
	CountQueryExecutorBuilder<T> count(Query<? extends Criterion> query);

	interface FindQueryExecutorBuilder<U extends Element> {
		FindQueryExecutorBuilder<U> find(Query<? extends Criterion> query);
		FindQueryExecutorBuilder<U> options(QueryOptionsMessenger options);
		FindQueryExecutorBuilder<U> xPath(String xPath) throws MetadataStoreException, DatastoreException;
		FindQueryExecutorBuilder<U> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException;
		MetadataQueryExecutor<U> execute() throws MetadataStoreException, DatastoreException;
	}

	interface CountQueryExecutorBuilder<V extends Element> {
		CountQueryExecutorBuilder<V> count(Query<? extends Criterion> query);
		CountQueryExecutorBuilder<V> xPath(String xPath) throws MetadataStoreException, DatastoreException;
		CountQueryExecutorBuilder<V> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException;
		long execute() throws MetadataStoreException, DatastoreException;
	}
}
