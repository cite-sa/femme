package gr.cite.femme.core.query.execution;

import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;

public interface ElementQueryExecutor<T extends Element> extends QueryExecutor<T> {
	MetadataQueryExecutor<T> options(QueryOptionsMessenger options);
	MetadataQueryExecutor<T> find(Query<? extends Criterion> query);
}
