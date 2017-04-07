package gr.cite.femme.metadatastore.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.datastore.mongodb.codecs.*;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoMetadataStoreClient {

	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataStoreClient.class);

//	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	private static final String DATABASE_HOST = "localhost";
	private static final int DATABASE_PORT = 27017;
	private static final String DATABASE_NAME = "femme-db-devel";
	private static final String METADATA_COLLECTION_NAME = "metadataJson";
	private static final String METADATA_GRIDFS_BUCKET_NAME = "metadataGridFS";

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<MetadatumJson> metadataJson;
	private GridFSBucket metadataGridFSBucket;
	private MongoCollection<MetadataGridFSFile> metadataGridFSFilesCollection;

	public MongoMetadataStoreClient() {
		this(MongoMetadataStoreClient.DATABASE_HOST, MongoMetadataStoreClient.DATABASE_PORT, MongoMetadataStoreClient.DATABASE_NAME, MongoMetadataStoreClient.METADATA_GRIDFS_BUCKET_NAME);
	}

	public MongoMetadataStoreClient(String host, int port, String name) {
		this(host, port, name, MongoMetadataStoreClient.METADATA_GRIDFS_BUCKET_NAME);
	}

	public MongoMetadataStoreClient(String host, int port, String name, String bucketName) {
		this.client = new MongoClient(host, port);
		this.database = client.getDatabase(name);
		
		CodecRegistry metadataJsonCodecRegistry = CodecRegistries
				.fromRegistries(CodecRegistries.fromProviders(new MetadatumJsonCodecProvider()), MongoClient.getDefaultCodecRegistry());

		CodecRegistry metadataGridFsFilesCodecRegistry = CodecRegistries
				.fromRegistries(
						CodecRegistries.fromProviders(new MetadataGridFSFileCodecProvider(), new MetadataGridFSFileMetadataCodecProvider()),
						MongoClient.getDefaultCodecRegistry());

		this.metadataJson = database.getCollection(MongoMetadataStoreClient.METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(metadataJsonCodecRegistry);
		this.metadataGridFSFilesCollection = database.getCollection(bucketName + ".files", MetadataGridFSFile.class)
				.withCodecRegistry(metadataGridFsFilesCodecRegistry);
		this.metadataGridFSBucket = GridFSBuckets.create(this.database, bucketName);
		
		createIndexes(bucketName);
	}

	public MongoCollection<MetadatumJson> getMetadataJson() {
		return this.metadataJson;
	}
	
	GridFSBucket getMetadataGridFSBucket() {
		return this.metadataGridFSBucket;
	}


	MongoCollection<MetadataGridFSFile> getMetadataGridFSFilesCollection() {
		return this.metadataGridFSFilesCollection;
	}

	void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}

	private void createIndexes(String bucketName) {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		this.database.getCollection(bucketName + "." + "files").createIndex(Indexes.ascending(FieldNames.METADATA_ELEMENT_ID_EMBEDDED, FieldNames.CHECKSUM));
	}
	
}
