package gr.cite.femme.core.query.execution;

import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;

import java.util.List;

public interface MetadataQueryExecutor<T extends Element> extends ElementQueryExecutor<T> {
	/*MetadataQueryExecutor<T> options(QueryOptionsMessenger options);
	MetadataQueryExecutor<T> find(Query<? extends Criterion> query);*/
	MetadataQueryExecutor<T> xPath(String xPath) throws DatastoreException, MetadataStoreException;
	MetadataQueryExecutor<T> xPath(List<String> elementIds, String xPath) throws DatastoreException, MetadataStoreException;
	MetadataQueryExecutor<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException;
	MetadataQueryExecutor<T> xPathInMemory(List<String> elementIds, String xPath) throws DatastoreException, MetadataStoreException;
}
