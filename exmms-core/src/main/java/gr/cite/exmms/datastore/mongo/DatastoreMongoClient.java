package gr.cite.exmms.datastore.mongo;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DatastoreMongoClient {
	private static final String DATABASE_NAME = "exmms-db";
	private static final String COLLECTION_NAME = "elements";
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	
	public DatastoreMongoClient() {
		client = new MongoClient();
		database = client.getDatabase(DATABASE_NAME);
		collection = database.getCollection(COLLECTION_NAME);
	}
	
	public MongoCollection<Document> getCollection() {
		return collection;
	}
	
	public void close() {
		client.close();
	}
}
