package gr.cite.femme.datastore.mongodb;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import gr.cite.femme.datastore.mongodb.codecs.ElementCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumXPathCacheCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJson;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJsonCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.SystemicMetadataCodecProvider;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public class MongoDatastoreClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastoreClient.class);
			
	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
//	private static final String DATABASE_HOST = "localhost:27017";
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
		this(DATABASE_HOST, DATABASE_NAME);
	}
	
	public MongoDatastoreClient(String dbHost, String dbName) {
		
		this.client = new MongoClient(dbHost);
		this.database = client.getDatabase(dbName);
		
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

		this.collections = database.getCollection(COLLECTIONS_COLLECTION_NAME, Collection.class).withCodecRegistry(codecRegistry);
		this.dataElements = database.getCollection(DATA_ELEMENTS_COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry);
		this.metadataJson = database.getCollection(METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(codecRegistry);
		this.metadataGridFS = GridFSBuckets.create(database, METADATA_BUCKET_NAME);
		
		createIndexes();
		
	}

	public MongoCollection<Collection> getCollections() {
		return this.collections;
	}
	
	public MongoCollection<DataElement> getDataElements() {
		return this.dataElements;
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
	
	private void createIndexes() {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		// Collections indexes
		this.database.getCollection(COLLECTIONS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.ENDPOINT), uniqueIndexOptions);
		this.database.getCollection(COLLECTIONS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.NAME), uniqueIndexOptions);
		
		// DataElements indexes
		this.database.getCollection(DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.ENDPOINT), uniqueIndexOptions);
		this.database.getCollection(DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.compoundIndex(
				Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.DATA_ELEMENT_COLLECTION_ENDPOINT)), uniqueIndexOptions);
		this.database.getCollection(DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.compoundIndex(
				Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.DATA_ELEMENT_COLLECTION_NAME)), uniqueIndexOptions);
		
		this.database.getCollection(METADATA_BUCKET_NAME + "." + "files").createIndex(Indexes.ascending(FieldNames.METADATA_ELEMENT_ID_EMBEDDED));
	}
	
}
