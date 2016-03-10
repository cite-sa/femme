package gr.cite.exmms.datastore.mongodb;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

import gr.cite.exmms.core.Element;
import gr.cite.exmms.datastore.mongodb.codecs.ElementCodecProvider;
import gr.cite.exmms.datastore.mongodb.codecs.MetadatumCodecProvider;

public class MongoDatastoreClient {
	private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "exmms-db";
	private static final String COLLECTION_NAME = "elements";
	private static final String METADATA_BUCKET_NAME = "metadata";
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Element> collection;
	private GridFSBucket gridFSBucket;
	
	public MongoDatastoreClient() {
		client = new MongoClient(DATABASE_HOST);
		database = client.getDatabase(DATABASE_NAME);
		gridFSBucket = GridFSBuckets.create(database, METADATA_BUCKET_NAME);
		
		/*MetadatumCodecProvider metadatumCodecProvider = new MetadatumCodecProvider();*/
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				CodecRegistries.fromProviders(new ElementCodecProvider(), new MetadatumCodecProvider(gridFSBucket)),
				MongoClient.getDefaultCodecRegistry()
			);
		/*MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry).build();*/
		
		/*client = new MongoClient(DATABASE_HOST, options);*/
		/*collection = database.getCollection(COLLECTION_NAME, Element.class);*/
		
		collection = database.getCollection(COLLECTION_NAME, Element.class).withCodecRegistry(codecRegistry);
		
		/*metadatumCodecProvider.setGridFSBucket(gridFSBucket);*/
	}
	
	public MongoCollection<Element> getElementsCollection() {
		return collection;
	}
	
	public GridFSBucket getGridFSBucket() {
		return gridFSBucket;
	}
	
	public void close() {
		client.close();
	}
}
