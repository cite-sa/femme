package gr.cite.femme.query.mongodb;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

public abstract class QueryExecutorFactory<T extends Element> {
	public abstract QueryExecutor<T> query(Datastore datastore, MetadataStore metadataStore, Class<T> elementSubtype);
}
