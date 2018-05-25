package gr.cite.femme.geo.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.geo.ServerGeo;
import gr.cite.femme.core.model.BBox;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.geo.api.GeoServiceApi;
import gr.cite.femme.geo.core.FemmeGeoException;
import gr.cite.femme.geo.core.GeoJson;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/")
@Component
public class FemmeGeoResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeGeoResource.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private FemmeClientAPI femmeClient;
	private MongoGeoDatastore geoDatastore;
	private GeoServiceApi geoServiceApi;
	
	@Inject
	public FemmeGeoResource(FemmeClientAPI femmeClient, MongoGeoDatastore geoDatastore, GeoServiceApi geoServiceApi) {
		this.femmeClient = femmeClient;
		this.geoDatastore = geoDatastore;
		this.geoServiceApi = geoServiceApi;
	}
	
	@GET
	@Path("servers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServersByCrs(@QueryParam("crs") String crs) {
		try {
			List<ServerGeo> servers = crs == null ? this.geoDatastore.getAllServers() : this.geoDatastore.getServersWithCrs(crs);
			return Response.ok(servers).build();
			
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
	}
	
	@GET
	@Path("servers/{serverId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServerById(@PathParam("serverId") String serverId) {
		try {
			ServerGeo server = this.geoDatastore.getServerById(serverId);
			
			return Response.ok(server).build();
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
	}
	
	@GET
	@Path("servers/{serverId}/coverages")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoveragesByServerId(@PathParam("serverId") String serverId) {
		List<CoverageGeo> coverages = this.geoDatastore.getCoveragesByServerId(serverId);
		setServerName(coverages);
		return Response.ok(coverages).build();
	}
	
	@GET
	@Path("coverages")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoveragesByDataElementId(@QueryParam("dataElementId") String dataElementId) {
		try {
			CoverageGeo coverage = this.geoDatastore.getCoverageByDataElementId(dataElementId);
			
			if (coverage == null) throw new NotFoundException("No coverage with dataElementId [" + dataElementId + "]");
			setServerName(coverage);
			
			return Response.ok(coverage).build();
		} catch (DatastoreException e) {
			logger.error("Error retrieving coverage with dataElementId [" + dataElementId + "]", e);
			throw new WebApplicationException("Error retrieving coverage with dataElementId [" + dataElementId + "]");
		}
	}
	
	@GET
	@Path("coverages/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoveragesByDataElementId(@QueryParam("id") List<String> ids) {
		try {
			List<CoverageGeo> coverages = this.geoDatastore.getCoveragesByIds(ids);
			
			if (coverages.size() == 0) throw new NotFoundException("No coverage found");
			setServerName(coverages);
			
			return Response.ok(coverages).build();
			
		} catch (DatastoreException e) {
			logger.error("Error retrieving coverages", e);
			throw new WebApplicationException("Error retrieving coverages");
		}
	}
	
	
	
	@GET
	@Path("coverages/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverageById(@PathParam("id") String id) {
		CoverageGeo coverage;
		try {
			coverage = this.geoDatastore.getCoverageById(id);
			setServerName(coverage);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage(), e);
		}
		
		if (coverage == null) throw new NotFoundException("No coverage exists [" + id + "]");
		
		return Response.ok(coverage).build();
	}
	
	@GET
	@Path("coverages/{id}/geo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverageGeo(@PathParam("id") String id) {
		CoverageGeo coverage;
		try {
			coverage = this.geoDatastore.getCoverageGeoById(id);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage(), e);
		}
		
		if (coverage == null) throw new NotFoundException("No coverage exists [" + id + "]");
		
		return Response.ok(coverage.getGeo()).build();
	}
	
	// BBOX = [minX, minY, maxX, maxY]- [minLongitude, minLatitude,maxLong, maxLat]
	@GET
	@Path("coverages/bbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverageByBBox(@QueryParam("bbox") String bbox) {
		if (bbox == null) return Response.status(Response.Status.BAD_REQUEST).build();
		
		try {
			List<CoverageGeo> coverageGeo = geoServiceApi.getCoveragesByBboxString(bbox);
			return Response.ok(coverageGeo).build();
		} catch (FemmeGeoException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage(), e);
		}
	}
	
	@POST
	@Path("coverages/intersects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoveragesIntersecting(GeoJson bbox) {
		if (bbox == null) return Response.status(Response.Status.BAD_REQUEST).build();
		
		List<CoverageGeo> coverages = this.geoDatastore.getCoveragesIntersectingOrWithin(bbox);
		setServerName(coverages);
		
		return Response.ok(coverages).build();
	}
	
	@GET
	@Path("coverages/point")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverageByPoint(@QueryParam("lat") Double latitude, @QueryParam("lon") Double longitude, @DefaultValue("0") @QueryParam("radius") Double radius) {
		if (latitude == null || longitude == null) throw new BadRequestException("lat/long cannot be null");

		try {
			List<CoverageGeo> coverageGeo = geoServiceApi.getCoveragesByPoint(longitude, latitude, radius);
			return Response.ok(coverageGeo).build();
		} catch (FemmeGeoException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage(), e);
		}
	}
	
	private void setServerName(List<CoverageGeo> coverages) {
		coverages.forEach(this::setServerName);
	}
	
	private void setServerName(CoverageGeo coverage) {
		try {
			ServerGeo server = this.geoDatastore.getServerById(coverage.getServerId());
			coverage.setServerName(server.getServerName());
		} catch (DatastoreException e) {
			logger.error("Error retrieving server for coverage [" + coverage.getId() + "]");
		}
	}
	
}
