package gr.cite.femme.engine.query.mongodb;

import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.QueryExecutor;

public class QueryExecutorProvider {
	public <T extends Element, R extends Datastore> QueryExecutor<T> get(Class<R> datastoreImplementation) {
		if (datastoreImplementation == MongoDatastore.class) {
			/*return new QueryMongoExecutor<>();*/
			return null;
		} else {
			return null;
		}
	}
}
