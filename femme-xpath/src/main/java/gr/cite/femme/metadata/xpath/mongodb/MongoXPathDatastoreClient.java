package gr.cite.femme.metadata.xpath.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import gr.cite.femme.metadata.xpath.mongodb.codecs.MaterializedPathsNodeCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoXPathDatastoreClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoXPathDatastoreClient.class);
			
//	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	private static final String DATABASE_HOST = "localhost:27017";
	private static final String DATABASE_NAME = "materialized-paths-db";
	private static final String MATERIALIZED_PATHS_COLLECTION_NAME = "paths";

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<MaterializedPathsNode> materializedPaths;

	public MongoXPathDatastoreClient() {
		this(DATABASE_HOST, DATABASE_NAME, MATERIALIZED_PATHS_COLLECTION_NAME);
	}
	
	public MongoXPathDatastoreClient(String dbHost, String dbName, String collectionName) {
		
		client = new MongoClient(dbHost);
		database = client.getDatabase(dbName);
		
		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(new MaterializedPathsNodeCodecProvider()),
				MongoClient.getDefaultCodecRegistry());

		materializedPaths = database.getCollection(MATERIALIZED_PATHS_COLLECTION_NAME, MaterializedPathsNode.class).withCodecRegistry(codecRegistry);

		createIndexes();
		
	}

	protected MongoCollection<MaterializedPathsNode> getMaterializedPaths() {
		return materializedPaths;
	}

	public void close() {
		logger.info("Closing connection to " + client.getAddress());
		client.close();
	}
	
	private void createIndexes() {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(false);
		
		this.database.getCollection(MATERIALIZED_PATHS_COLLECTION_NAME).createIndex(Indexes.ascending("path"), uniqueIndexOptions);
		this.database.getCollection(MATERIALIZED_PATHS_COLLECTION_NAME).createIndex(Indexes.ascending("@.dimension"), uniqueIndexOptions);
	}
	
}
