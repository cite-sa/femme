package gr.cite.femme.geo.engine.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import gr.cite.femme.core.datastores.Datastore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.geo.ServerGeo;
import gr.cite.femme.geo.mongodb.codecs.CoverageGeoCodec;
import gr.cite.femme.geo.mongodb.codecs.ServerGeoCodec;
import gr.cite.femme.geo.utils.GeoUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoGeoDatastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoGeoDatastore.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private MongoGeoDatastoreClient mongoClient;
	
	/*public MongoGeoDatastore() {
		this.mongoClient = new MongoGeoDatastoreClient();
	}*/
	
	@Inject
	public MongoGeoDatastore(MongoGeoDatastoreClient mongoClient) {
		this.mongoClient = mongoClient;
	}
	
	public void close() {
		this.mongoClient.close();
	}
	
	public String insert(ServerGeo server) throws DatastoreException {
		ServerGeo serverGeo = null;
		if (server.getCollectionId() != null) {
			serverGeo = getServerByCollectionId(server.getCollectionId());
		}
		
		if (serverGeo != null) {
			server.setId(serverGeo.getId());
			logger.info("Server already exists [" + server.getId() + "]");
			
			return null;
		} else {
			server.setCreated(Instant.now());
			server.setModified(Instant.now());
			
			this.mongoClient.getServers().insertOne(server);
			
			return server.getId();
		}
	}
	
	public String insert(CoverageGeo coverage) throws DatastoreException {
		//if (coverage.getGeo() != null) {
		//	this.mongoClient.getCoverages().createIndex(new BasicDBObject("loc", "2dsphere"));
		//}
		
		CoverageGeo coverageGeo = null;
		if (coverage.getDataElementId() != null) {
			coverageGeo = getCoverageByDataElementId(coverage.getDataElementId());
		}
		
		if (coverageGeo != null) {
			logger.info("Coverage already exists [" + coverage.getId() + "]");
			coverage = updateCoverage(coverage, coverageGeo.getId());
			return coverage.getId();
		} else {
			coverage.setCreated(Instant.now());
			coverage.setModified(Instant.now());
			
			this.mongoClient.getCoverages().insertOne(coverage);
			
			return coverage.getId();
		}
	}
	
	private CoverageGeo updateCoverage(CoverageGeo coverageGeo, String id) throws DatastoreException {
		CoverageGeo updated = null;
		
		if (coverageGeo.getId() != null) {
			coverageGeo.setModified(Instant.now());
			
			try {
				updated = this.mongoClient.getCoverages().findOneAndUpdate(
						Filters.eq(CoverageGeoCodec.ID, generateId(id)),
						new Document().append("$set", coverageGeo),
						new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
			} catch (Exception e) {
				throw new DatastoreException("Error on " + coverageGeo.getClass() + " [" + coverageGeo.getId() + "] update", e);
			}
		}
		return updated;
	}
	
	public List<ServerGeo> getAllServers() throws DatastoreException {
		List<ServerGeo> servers = new ArrayList<>();
		
		try {
			this.mongoClient.getServers().find().into(servers);
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
		
		return servers;
	}
	
	public ServerGeo getServerById(String id) throws DatastoreException {
		try {
			return this.mongoClient.getServers().find(Filters.eq(ServerGeoCodec.ID, generateId(id))).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public ServerGeo getServerByCollectionId(String collectionId) throws DatastoreException {
		try {
			return this.mongoClient.getServers().find(Filters.eq(ServerGeoCodec.COLLECTION_ID, generateId(collectionId))).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public List<ServerGeo> getServersWithCrs(String crs) throws DatastoreException {
		List<ServerGeo> serversByCrs = new ArrayList<>();
		this.mongoClient.getCoveragesWithServerCodec().aggregate(
				Arrays.asList(
						Aggregates.match(Filters.eq(CoverageGeoCodec.CRS, crs)),
						Aggregates.group("$" + CoverageGeoCodec.SERVER_ID)
				)
		).into(serversByCrs);
		
		List<ServerGeo> servers = new ArrayList<>();
		List<Bson> getServerById = serversByCrs.stream().map(server -> Filters.eq(ServerGeoCodec.ID, generateId(server.getId()))).collect(Collectors.toList());
		this.mongoClient.getServers().find(Filters.or(getServerById)).into(servers);
		
		return servers;
	}
	
	public List<CoverageGeo> getAllCoverages() throws DatastoreException {
		List<CoverageGeo> coverages = new ArrayList<>();
		
		try {
			this.mongoClient.getCoverages().find().into(coverages);
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
		
		return coverages;
	}
	
	public CoverageGeo getCoverageById(String id) throws DatastoreException {
		try {
			return this.mongoClient.getCoverages().find(Filters.eq(CoverageGeoCodec.ID, generateId(id))).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public CoverageGeo getCoverageGeoById(String id) throws DatastoreException {
		try {
			return this.mongoClient.getCoverages().find(Filters.eq(CoverageGeoCodec.ID, generateId(id))).projection(Projections.include(CoverageGeoCodec.LOC)).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public CoverageGeo getCoverageByDataElementId(String dataElementId) throws DatastoreException {
		try {
			return this.mongoClient.getCoverages().find(Filters.eq(CoverageGeoCodec.DATA_ELEMENT_ID, generateId(dataElementId))).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public CoverageGeo getCoverageByName(String name) throws DatastoreException {
		try {
			return this.mongoClient.getCoverages().find(Filters.eq(CoverageGeoCodec.COVERAGE_NAME, name)).first();
		} catch (Exception e) {
			throw new DatastoreException(e);
		}
	}
	
	public List<CoverageGeo> getCoveragesByPolygon(GeoJsonObject geoJson) throws IOException {
		String jsonString = mapper.writeValueAsString(geoJson);
		String query = GeoUtils.buildGeoWithinQuery(jsonString);
		logger.debug("query:" + query);
		
		List<CoverageGeo> results = new ArrayList<>();
		this.mongoClient.getCoverages().find(Document.parse(query)).into(results);
		
		return results;
	}
	
	public List<CoverageGeo> getCoverageByCoords(double longitude, double latitude, double radius) {
		Point refPoint = new Point(new Position(longitude, latitude));
		return this.mongoClient.getCoverages().find(Filters.near(CoverageGeoCodec.LOC, refPoint, radius, radius)).into(new ArrayList<>());
	}
	
	public List<CoverageGeo> getCoveragesByServerId(String serverId) {
		List<CoverageGeo> coverages = new ArrayList<>();
		this.mongoClient.getCoverages().find(Filters.eq(CoverageGeoCodec.SERVER_ID, generateId(serverId))).into(coverages);
		return coverages;
	}
	
	public String generateId() {
		return new ObjectId().toString();
	}
	
	public ObjectId generateId(String id) {
		return new ObjectId(id);
	}
}
