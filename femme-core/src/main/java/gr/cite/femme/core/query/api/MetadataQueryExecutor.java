package gr.cite.femme.core.query.api;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;

import java.util.List;

public interface MetadataQueryExecutor<T extends Element> extends QueryExecutor<T> {

	public MetadataQueryExecutor<T> options(QueryOptionsMessenger options);

	public MetadataQueryExecutor<T> find(Query<? extends Criterion> query);

	public MetadataQueryExecutor<T> xPath(String xPath) throws DatastoreException, MetadataStoreException;

	/*public List<T> list() throws DatastoreException, MetadataStoreException;

	public T first() throws DatastoreException, MetadataStoreException;*/
}
