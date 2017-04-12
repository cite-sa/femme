package gr.cite.femme.engine.query.mongodb;

import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.QueryExecutor;

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
