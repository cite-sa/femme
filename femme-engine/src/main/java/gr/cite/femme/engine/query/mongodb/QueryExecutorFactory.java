package gr.cite.femme.engine.query.mongodb;

import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.QueryExecutor;

public abstract class QueryExecutorFactory<T extends Element> {
	public abstract QueryExecutor<T> query(Datastore datastore, MetadataStore metadataStore, Class<T> elementSubtype);
}
