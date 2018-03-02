package gr.cite.femme.geo.engine.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.model.BBox;
import gr.cite.femme.geo.core.ServerGeo;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gr.cite.femme.geo.utils.GeoUtils.buildGeoWithinQuery;

@Component
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

		if ( coverage.getGeo()!= null){
			this.mongoClient.getCoverages().createIndex( new BasicDBObject("loc", "2dsphere"));
		}
		CoverageGeo coverageGeo = 	this.mongoClient.getCoverages().find(new Document("_id", coverage.getCoverageId())).first();

		if(coverageGeo!=null){
			System.out.println("*********************** Coverage already present ***********************");
			return "exists";
		} else {
			this.mongoClient.getCoverages().insertOne(coverage);
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

	public List<CoverageGeo> getCoveragesByPolygon(GeoJsonObject geoJson) throws DatastoreException, IOException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		String jsonString = mapper.writeValueAsString(geoJson);
		String query = buildGeoWithinQuery(jsonString);
		System.out.println("->"+query);
		List<CoverageGeo> results = collection.find(Document.parse(query)).into(new ArrayList<CoverageGeo>());

		return results;
	}

	public CoverageGeo getCoverageByBBox(GeoJsonObject geoJson) throws DatastoreException, JsonProcessingException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		String jsonString = mapper.writeValueAsString(geoJson);

		//collection.find(jsonString);
		return null;
	}

	public List<CoverageGeo> getCoverageByCoords(double longitude, double latitude, double radius) throws DatastoreException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		Point refPoint = new Point(new Position(longitude, latitude));
		return collection.find(Filters.near("loc", refPoint, radius, 0.0)).into(new ArrayList<CoverageGeo>());
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
