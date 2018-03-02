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
import gr.cite.femme.geo.core.ServerGeo;
import gr.cite.femme.geo.utils.GeoUtils;
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

@Component
public class MongoGeoDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoGeoDatastore.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
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
	
	public String insert(CoverageGeo coverage) {
		
		if (coverage.getGeo() != null) {
			this.mongoClient.getCoverages().createIndex(new BasicDBObject("loc", "2dsphere"));
		}
		CoverageGeo coverageGeo = this.mongoClient.getCoverages().find(new Document("_id", coverage.getCoverageId())).first();
		
		if (coverageGeo != null) {
			System.out.println("*********************** Coverage already present ***********************");
			return null;
		} else {
			this.mongoClient.getCoverages().insertOne(coverage);
			return coverage.getId();
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
	
	public List<CoverageGeo> getCoveragesByPolygon(GeoJsonObject geoJson) throws IOException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		
		String jsonString = mapper.writeValueAsString(geoJson);
		String query = GeoUtils.buildGeoWithinQuery(jsonString);
		logger.debug("->" + query);
		
		List<CoverageGeo> results = new ArrayList<>();
		collection.find(Document.parse(query)).into(results);
		
		/*Iterable<CoverageGeo> geoIterable = collection.find();
		for (CoverageGeo c : geoIterable) {
			logger.debug(c.getId());
		}*/
		
		return results;
	}
	
	public CoverageGeo getCoverageByBBox(GeoJsonObject geoJson) throws JsonProcessingException {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		String jsonString = mapper.writeValueAsString(geoJson);
		
		//collection.find(jsonString);
		return null;
	}
	
	public List<CoverageGeo> getCoverageByCoords(double longitude, double latitude, double radius) {
		MongoCollection<CoverageGeo> collection = this.mongoClient.getCoverages();
		Point refPoint = new Point(new Position(longitude, latitude));
		
		return collection.find(Filters.near("loc", refPoint, radius, radius)).into(new ArrayList<>());
		
	}
	
	
	public List<CoverageGeo> getCoveragesInServer(String serverId) {
		return null;
	}
	
	public String generateId() {
		return new ObjectId().toString();
	}
	
	public Object generateId(String id) {
		return new ObjectId(id);
	}
}
