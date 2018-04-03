package gr.cite.femme.geo.resources;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.geo.ServerGeo;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/admin")
public class FemmeGeoAdminResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeGeoAdminResource.class);
	
	private MongoGeoDatastore geoDatastore;
	
	@Inject
	public FemmeGeoAdminResource(MongoGeoDatastore geoDatastore) {
		this.geoDatastore = geoDatastore;
	}
	
	@POST
	@Path("servers")
	public Response insertServer(ServerGeo server) throws DatastoreException {
		try {
			String id = this.geoDatastore.insertServer(server);
			logger.info("Server [" + server.getId() + "] successfully inserted");
			if (id != null) {
				logger.info("Server [" + server.getId() + "] successfully inserted");
				return Response.ok().entity(id).build();
			} else {
				logger.info("Server [" + server.getId() + "] already exists");
				return Response.ok().build();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException("Error inserting server [" + server.getId() + "]", e);
		}
	}
	
	@POST
	@Path("coverages")
	public Response insertCoverage(CoverageGeo coverage) {
		try {
			String id = this.geoDatastore.insert(coverage);
			
			if (id != null) {
				logger.info("Coverage [" + coverage.getId() + "] successfully inserted");
				return Response.ok().entity(id).build();
			} else {
				logger.info("Coverage [" + coverage.getId() + "] already exists");
				return Response.ok().build();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException("Error inserting coverage [" + coverage.getCoverageName() + "]", e);
		}
	}
}
