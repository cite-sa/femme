package gr.cite.femme.geo.engine.mongodb;

import com.mongodb.client.MongoCollection;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.geo.core.CoverageGeo;
import gr.cite.femme.geo.core.ServerGeo;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class MongoGeoDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoGeoDatastore.class);
	private MongoGeoDatastoreClient mongoClient;


	public MongoGeoDatastore() {
		this.mongoClient = new MongoGeoDatastoreClient();
	}

	@Inject
	public MongoGeoDatastore(String host, int port, String name) {
		this.mongoClient = new MongoGeoDatastoreClient(host, port, name);
	}
	
	public MongoCollection<ServerGeo> getServers() {
		return this.mongoClient.getServers();
	}

	public MongoCollection<CoverageGeo> getCoverages() {
		return this.mongoClient.getCoverages();
	}

	public void close() {
		this.mongoClient.close();
	}

	public String insert(ServerGeo server) throws DatastoreException {
		return null;
	}
	
	public String insert(CoverageGeo coverage) throws DatastoreException {
		return null;
	}
	
	public ServerGeo getServerById(String id) throws DatastoreException {
		return null;
	}
	
	public ServerGeo getServerByName(String name) throws DatastoreException {
		return null;
	}
	
	public CoverageGeo getCoverageById(String id) throws DatastoreException {
		return null;
	}

	public CoverageGeo getCoverageByName(String name) throws DatastoreException {
		return null;
	}
	
	public List<CoverageGeo> getCoveragesInServer(String serverId) throws DatastoreException {
		return null;
	}

	public String generateId() {
		return new ObjectId().toString();
	}

	public Object generateId(String id) {
		return new ObjectId(id);
	}
}
