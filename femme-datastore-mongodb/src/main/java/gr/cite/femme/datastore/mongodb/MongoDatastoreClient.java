package gr.cite.femme.datastore.mongodb;

import gr.cite.femme.model.Element;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import gr.cite.femme.datastore.mongodb.codecs.ElementCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumXPathCacheCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.MetadatumJsonCodecProvider;
import gr.cite.femme.datastore.mongodb.codecs.SystemicMetadataCodecProvider;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public class MongoDatastoreClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastoreClient.class);
			
//	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	private static final String DATABASE_HOST = "localhost";
	private static final int DATABASE_PORT = 27017;
	private static final String DATABASE_NAME = "femme-db";
	private static final String COLLECTIONS_COLLECTION_NAME = "collections";
	private static final String DATA_ELEMENTS_COLLECTION_NAME = "dataElements";

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Collection> collections;
	private MongoCollection<DataElement> dataElements;

	public MongoDatastoreClient() {
		this(MongoDatastoreClient.DATABASE_HOST, MongoDatastoreClient.DATABASE_PORT, MongoDatastoreClient.DATABASE_NAME);
	}
	
	public MongoDatastoreClient(String host, int port, String name) {
		
		this.client = new MongoClient(host, port);
		this.database = client.getDatabase(name);
		
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

		this.collections = database.getCollection(MongoDatastoreClient.COLLECTIONS_COLLECTION_NAME, Collection.class).withCodecRegistry(codecRegistry);
		this.dataElements = database.getCollection(MongoDatastoreClient.DATA_ELEMENTS_COLLECTION_NAME, DataElement.class).withCodecRegistry(codecRegistry);
		/*this.metadataJson = database.getCollection(METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(codecRegistry);
		this.metadataGridFS = GridFSBuckets.create(database, METADATA_BUCKET_NAME);*/
		
		createIndexes();
		
	}

	<T extends Element> MongoCollection<T> getCollection(Class<T> elementSubType) throws IllegalArgumentException {
		if (elementSubType != null && elementSubType.equals(Collection.class)) {
			return (MongoCollection<T>) this.collections;
		} else if (elementSubType != null && elementSubType.equals(DataElement.class)) {
			return (MongoCollection<T>) this.dataElements;
		} else {
			throw new IllegalArgumentException(elementSubType == null ? "null" : elementSubType.getSimpleName() + " collection does not exist");
		}
	}

	MongoCollection<Collection> getCollections() {
		return this.collections;
	}
	
	MongoCollection<DataElement> getDataElements() {
		return this.dataElements;
	}
	
	/*public MongoCollection<MetadatumJson> getMetadataJson() {
		return this.metadataJson;
	}
	
	public GridFSBucket getMetadataGridFSBucket() {
		return this.metadataGridFS;
	}*/

	void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}
	
	private void createIndexes() {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);
		
		//Collections indices
		this.database.getCollection(MongoDatastoreClient.COLLECTIONS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.ENDPOINT, FieldNames.NAME), uniqueIndexOptions);

		//DataElements indices
		this.database.getCollection(MongoDatastoreClient.DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.ascending(FieldNames.ENDPOINT), uniqueIndexOptions);
		this.database.getCollection(MongoDatastoreClient.DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.compoundIndex(
				Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.DATA_ELEMENT_COLLECTION_ENDPOINT)), uniqueIndexOptions);
		this.database.getCollection(MongoDatastoreClient.DATA_ELEMENTS_COLLECTION_NAME).createIndex(Indexes.compoundIndex(
				Indexes.ascending(FieldNames.NAME), Indexes.ascending(FieldNames.DATA_ELEMENT_COLLECTION_NAME)), uniqueIndexOptions);
		
		/*this.database.getCollection(METADATA_BUCKET_NAME + "." + "files").createIndex(Indices.ascending(FieldNames.METADATA_ELEMENT_ID_EMBEDDED));*/
	}
	
}
