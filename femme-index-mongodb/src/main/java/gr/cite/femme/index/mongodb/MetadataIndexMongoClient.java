package gr.cite.femme.index.mongodb;

import gr.cite.femme.index.mongodb.codecs.MetadatumIndexCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gr.cite.femme.index.api.client.MetadatumIndex;

public class MetadataIndexMongoClient {

	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	//private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "femme-db";
	private static final String METADATA_INDEX_COLLECTION_NAME = "metadataIndex";

	private MongoClient client;

	private MongoDatabase database;

	private MongoCollection<MetadatumIndex> metadataIndexCollection;

	public MetadataIndexMongoClient() {
		this(DATABASE_HOST, DATABASE_NAME);
	}
	
	public MetadataIndexMongoClient(String host, String dbName) {

		client = new MongoClient(host);
		database = client.getDatabase(dbName);

		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(new MetadatumIndexCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		metadataIndexCollection = database.getCollection(METADATA_INDEX_COLLECTION_NAME, MetadatumIndex.class)
				.withCodecRegistry(codecRegistry);
	}
	
	public void close() {
		client.close();
	}
	
	public MongoCollection<MetadatumIndex> getMetadataIndexCollection() {
		return metadataIndexCollection;
	}
}
