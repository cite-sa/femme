package gr.cite.femme.core.query.execution;

import java.util.List;

import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;

public interface QueryExecutor<T extends Element> {
	QueryExecutor<T> options(QueryOptionsMessenger options);
	QueryExecutor<T> find(Query<? extends Criterion> query);
	long count(Query<? extends Criterion> query);
	List<T> list() throws DatastoreException, MetadataStoreException;
	T first() throws DatastoreException, MetadataStoreException;
}
