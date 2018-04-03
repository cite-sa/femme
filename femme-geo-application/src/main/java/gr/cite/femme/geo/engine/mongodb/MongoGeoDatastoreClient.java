package gr.cite.femme.geo.engine.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.geo.ServerGeo;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;

import gr.cite.femme.geo.mongodb.codecs.CoverageGeoCodecProvider;
import gr.cite.femme.geo.mongodb.codecs.ServerGeoCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoGeoDatastoreClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoGeoDatastoreClient.class);
	
	//	private static final String DATABASE_HOST = "es-devel1.local.cite.gr:27017";
	private static final String DATABASE_HOST = "localhost";
	private static final int DATABASE_PORT = 27017;
	private static final String DATABASE_NAME = "femme-geo-db-devel";
	private static final String SERVER_COLLECTION_NAME = "servers";
	private static final String COVERAGE_COLLECTION_NAME = "coverages";
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<ServerGeo> servers;
	private MongoCollection<CoverageGeo> coverages;
	
	public MongoGeoDatastoreClient() {
		this(MongoGeoDatastoreClient.DATABASE_HOST, MongoGeoDatastoreClient.DATABASE_PORT, MongoGeoDatastoreClient.DATABASE_NAME);
	}


	
	public MongoGeoDatastoreClient(String host, int port, String name) {
		
		this.client = new MongoClient(host, port);
		this.database = client.getDatabase(name);
		
		CodecRegistry codecRegistry = CodecRegistries
				.fromRegistries(
						MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(
								new ServerGeoCodecProvider(),
								new CoverageGeoCodecProvider()),
						MongoClient.getDefaultCodecRegistry());
		
		this.servers = this.database.getCollection(MongoGeoDatastoreClient.SERVER_COLLECTION_NAME, ServerGeo.class).withCodecRegistry(codecRegistry);
		this.coverages = this.database.getCollection(MongoGeoDatastoreClient.COVERAGE_COLLECTION_NAME, CoverageGeo.class).withCodecRegistry(codecRegistry);
	}

	/*<T extends Element> MongoCollection<T> getCollection(Class<T> elementSubType) throws IllegalArgumentException {
		if (elementSubType != null && elementSubType.equals(ServerGeo.class)) {
			return (MongoCollection<T>) this.servers;
		} else if (elementSubType != null && elementSubType.equals(CoverageGeo.class)) {
			return (MongoCollection<T>) this.coverages;
		} else {
			throw new IllegalArgumentException(elementSubType == null ? "null" : elementSubType.getSimpleName() + " collection does not exist");
		}
	}*/
	
	MongoCollection<ServerGeo> getServers() {
		return this.servers;
	}
	
	MongoCollection<CoverageGeo> getCoverages() {
		return this.coverages;
	}
	
	void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}
}