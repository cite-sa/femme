package gr.cite.femme.datastore.mongodb.metadata;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.datastore.mongodb.codecs.*;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoMetadataStoreClient {

	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataStoreClient.class);

//	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "metadata-db";
	private static final String METADATA_COLLECTION_NAME = "metadataJson";
	private static final String METADATA_BUCKET_NAME = "metadataGridFS";
	/*private static final String METADATA_INDEX_COLLECTION_NAME = "metadataIndex";*/

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<MetadatumJson> metadataJson;
	private GridFSBucket metadataGridFS;

	public MongoMetadataStoreClient() {
		this(MongoMetadataStoreClient.DATABASE_HOST, MongoMetadataStoreClient.DATABASE_NAME,MongoMetadataStoreClient.METADATA_BUCKET_NAME);
	}

	public MongoMetadataStoreClient(String dbHost, String dbName, String bucketName) {
		
		this.client = new MongoClient(dbHost);
		this.database = client.getDatabase(dbName);
		
		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(
								new MetadatumJsonCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		this.metadataJson = database.getCollection(METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(codecRegistry);
		this.metadataGridFS = GridFSBuckets.create(database, bucketName);
		
		createIndexes(bucketName);
		
	}

	public MongoCollection<MetadatumJson> getMetadataJson() {
		return this.metadataJson;
	}
	
	public GridFSBucket getMetadataGridFS() {
		return this.metadataGridFS;
	}

	public void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}
	
	private void createIndexes(String bucketName) {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		this.database.getCollection(bucketName + "." + "files").createIndex(Indexes.ascending(FieldNames.METADATA_ELEMENT_ID_EMBEDDED));
	}
	
}
