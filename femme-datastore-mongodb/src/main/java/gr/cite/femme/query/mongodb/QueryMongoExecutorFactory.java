package gr.cite.femme.query.mongodb;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

public class QueryMongoExecutorFactory<T extends Element> extends QueryExecutorFactory<T> {

	@Override
	public QueryExecutor<T> query(Datastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		if (datastore instanceof MongoDatastore) {
			return new QueryMongoExecutor<>((MongoDatastore) datastore, metadataStore, elementSubtype);
		} else {
			return null;
		}
	}
}
