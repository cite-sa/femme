package gr.cite.femme.query.mongodb;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

public class QueryExecutorFactoryProducer {
	public static <U extends Datastore, V extends Element> QueryExecutorFactory<V> getFactory(Class<U> datastoreImplementation) {
		if (datastoreImplementation == MongoDatastore.class) {
			return new QueryMongoExecutorFactory<>();
		} else {
			return null;
		}
	}
}
