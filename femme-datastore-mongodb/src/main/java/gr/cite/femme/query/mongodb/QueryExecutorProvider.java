package gr.cite.femme.query.mongodb;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;

import javax.xml.crypto.Data;

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
