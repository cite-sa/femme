package gr.cite.femme.datastore.mongodb;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.mongodb.codecs.ElementCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.SystemicMetadataCodecProvider;

public class MongoDatastoreClient {
	private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "femme-db";
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

		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						CodecRegistries.fromProviders(
								new ElementCodecProvider(),
								new MetadatumCodecProvider(gridFSBucket),
								new SystemicMetadataCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		collection = database.getCollection(COLLECTION_NAME, Element.class).withCodecRegistry(codecRegistry);
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
