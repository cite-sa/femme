package gr.cite.femme.geo.engine.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.result.UpdateResult;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.geo.ServerGeo;
import gr.cite.femme.geo.utils.GeoUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
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
	
	public String insert(CoverageGeo coverage) throws DatastoreException {
		
		if (coverage.getGeo() != null) {
			this.mongoClient.getCoverages().createIndex(new BasicDBObject("loc", "2dsphere"));
		}
		CoverageGeo coverageGeo = this.mongoClient.getCoverages().find(new Document("dataElementId", coverage.getDataElementId())).first();
		
		if (coverageGeo != null) {
			System.out.println("*********************** Coverage already present ***********************");
			coverage = updateCoverage(coverage,coverageGeo.getId());
			return coverage.getId();
		} else {
			this.mongoClient.getCoverages().insertOne(coverage);
			return coverage.getId();
		}
	}

	public CoverageGeo updateCoverage(CoverageGeo coverageGeo, String id) throws DatastoreException {
		CoverageGeo updated = null;

		if (coverageGeo.getId() != null) {
			coverageGeo.setModified(Instant.now());

			try {
				updated = (CoverageGeo) this.mongoClient.getCoverages().findOneAndUpdate(
						Filters.eq("_id", new ObjectId(id)),
						new Document().append("$set", coverageGeo),
						new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
			} catch (Exception e) {
				throw new DatastoreException("Error on " + coverageGeo.getClass() + " [" + coverageGeo.getId() + "] update", e);
			}
		}
		return updated;
	}

	public String insertServer(ServerGeo server) {

		ServerGeo serverGeo = this.mongoClient.getServers().find(new Document("collectionId", server.getId())).first();

		if (serverGeo != null) {
			System.out.println("*********************** Server already present ***********************");
			return null;
		} else {
			this.mongoClient.getServers().insertOne(server);
			return server.getId();
		}
	}
	
	public ServerGeo getServerById(String id) throws DatastoreException {
		return this.mongoClient.getServers().find(new Document("collectionId", id)).first();
	}
	
	public ServerGeo getServerByCollectionId(String name) throws DatastoreException {
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
		logger.debug("query:" + query);
		List<CoverageGeo> results = new ArrayList<>();
		collection.find(Document.parse(query)).into(results);

		/*Iterable<CoverageGeo> geoIterable = collection.find();
		for (CoverageGeo c : geoIterable) {
			logger.debug(c.getId());
		}*/
		
		return results;
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
