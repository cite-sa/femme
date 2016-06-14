package gr.cite.femme.datastore.mongodb;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.datastore.mongodb.codecs.ElementCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumXPathCacheCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJson;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJsonCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.SystemicMetadataCodecProvider;

public class MongoDatastoreClient {
	/*private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";*/
	private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "femme-db";
	private static final String COLLECTIONS_COLLECTION_NAME = "collections";
	private static final String DATA_ELEMENTS_COLLECTION_NAME = "dataElements";
	private static final String METADATA_COLLECTION_NAME = "metadataJson";
	private static final String METADATA_BUCKET_NAME = "metadataGridFS";
	/*private static final String METADATA_INDEX_COLLECTION_NAME = "metadataIndex";*/

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Collection> collections;
	private MongoCollection<DataElement> dataElements;
	private MongoCollection<MetadatumJson> metadataJson;
	private GridFSBucket metadataGridFS;
	/*private MongoCollection<MetadatumXPathCache> metadataIndex;*/
	
	public MongoDatastoreClient() {
		
		client = new MongoClient(DATABASE_HOST);
		database = client.getDatabase(DATABASE_NAME);
		
		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(
								new ElementCodecProvider(),
								new MetadatumCodecProvider(),
								new MetadatumXPathCacheCodecProvider(),
								new MetadatumJsonCodecProvider(),
								new SystemicMetadataCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		collections = database.getCollection(COLLECTIONS_COLLECTION_NAME, Collection.class).withCodecRegistry(codecRegistry);
		dataElements = database.getCollection(DATA_ELEMENTS_COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry);
		metadataJson = database.getCollection(METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(codecRegistry);
		metadataGridFS = GridFSBuckets.create(database, METADATA_BUCKET_NAME);
	}
	
	public MongoDatastoreClient(String dbHost, String dbName) {
		
		client = new MongoClient(dbHost);
		database = client.getDatabase(dbName);
		
		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(
								new ElementCodecProvider(),
								new MetadatumCodecProvider(),
								new MetadatumXPathCacheCodecProvider(),
								new MetadatumJsonCodecProvider(),
								new SystemicMetadataCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		collections = database.getCollection(COLLECTIONS_COLLECTION_NAME, Collection.class).withCodecRegistry(codecRegistry);
		dataElements = database.getCollection(DATA_ELEMENTS_COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry);
		metadataJson = database.getCollection(METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(codecRegistry);
		metadataGridFS = GridFSBuckets.create(database, METADATA_BUCKET_NAME);
	}

	public MongoCollection<Collection> getCollections() {
		return collections;
	}
	
	public MongoCollection<DataElement> getDataElements() {
		return dataElements;
	}
	
	public MongoCollection<MetadatumJson> getMetadataJson() {
		return metadataJson;
	}
	
	public GridFSBucket getMetadataGridFS() {
		return metadataGridFS;
	}

	public void close() {
		client.close();
	}
}
