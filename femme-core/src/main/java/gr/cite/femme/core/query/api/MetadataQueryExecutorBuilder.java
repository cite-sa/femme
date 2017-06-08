package gr.cite.femme.core.query.api;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;

public interface MetadataQueryExecutorBuilder<T extends Element> {
	public MetadataQueryExecutorBuilder<T> options(QueryOptionsMessenger options);

	public MetadataQueryExecutorBuilder<T> find(Query<? extends Criterion> query);

	public MetadataQueryExecutorBuilder<T> xPath(String xPath) throws DatastoreException, MetadataStoreException;

	public MetadataQueryExecutorBuilder<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException;

	public MetadataQueryExecutor<T> build() throws MetadataStoreException, DatastoreException;
}
