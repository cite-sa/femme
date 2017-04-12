package gr.cite.femme.engine.query.mongodb;

import gr.cite.femme.api.Datastore;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.core.model.Element;

public class QueryExecutorFactoryProducer {
	public static <U extends Datastore, V extends Element> QueryExecutorFactory<V> getFactory(Class<U> datastoreImplementation) {
		if (datastoreImplementation == MongoDatastore.class) {
			return new QueryMongoExecutorFactory<>();
		} else {
			return null;
		}
	}
}
