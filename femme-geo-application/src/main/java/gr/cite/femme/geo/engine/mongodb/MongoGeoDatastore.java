package gr.cite.femme.geo.engine.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.geo.core.ServerGeo;
import org.bson.types.ObjectId;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;


public class MongoGeoDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoGeoDatastore.class);
	private MongoGeoDatastoreClient mongoClient;
	private static final ObjectMapper mapper = new ObjectMapper();


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

		System.out.println("BSON:"+new BasicDBObject("geometries", "2dsphere"));
		if ( coverage.getGeo()!= null){
			this.mongoClient.getCoverages().createIndex( new BasicDBObject("geometries", "2dsphere"));
		}
		CoverageGeo coverageGeo = 	this.mongoClient.getCoverages().find().first();
		if(coverageGeo!=null){
			System.out.println("*********************** Coverage already present ***********************");
			return "exists";
		} else {
			this.mongoClient.getCoverages().insertOne(coverageGeo);
			return "inserted";
		}
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
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		collection.find();
		return null;
	}

	public CoverageGeo getCoverageByPolygon(GeoJsonObject geoJson) throws DatastoreException, JsonProcessingException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		String jsonString = mapper.writeValueAsString(geoJson);

		//collection.find(jsonString);
		return null;
	}

	public CoverageGeo getCoverageByBBox(GeoJsonObject geoJson) throws DatastoreException, JsonProcessingException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		String jsonString = mapper.writeValueAsString(geoJson);

		//collection.find(jsonString);
		return null;
	}

	public CoverageGeo getCoverageByCoords(double latitude, double longitude, double radius) throws DatastoreException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		Point refPoint = new Point(new Position(latitude, longitude));
		//collection.find(Filters.near("contact.location", refPoint, radius, radius)).forEach();
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
