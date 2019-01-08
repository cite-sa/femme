package gr.cite.femme.core.query.execution;

import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;

public interface ElementQueryExecutorBuilder<T extends Element> {
	MetadataQueryExecutorBuilder.FindQueryExecutorBuilder<T> find();
	MetadataQueryExecutorBuilder.FindQueryExecutorBuilder<T> find(Query<? extends Criterion> query);
	MetadataQueryExecutorBuilder.CountQueryExecutorBuilder<T> count();
	MetadataQueryExecutorBuilder.CountQueryExecutorBuilder<T> count(Query<? extends Criterion> query);
	
	interface FindQueryExecutorBuilder<U extends Element> {
		FindQueryExecutorBuilder<U> find();
		FindQueryExecutorBuilder<U> find(Query<? extends Criterion> query);
		FindQueryExecutorBuilder<U> options(QueryOptionsMessenger options);
		ElementQueryExecutor<U> execute() throws MetadataStoreException, DatastoreException;
	}
	
	interface CountQueryExecutorBuilder<V extends Element> {
		CountQueryExecutorBuilder<V> count(Query<? extends Criterion> query);
		long execute() throws MetadataStoreException, DatastoreException;
	}
}
