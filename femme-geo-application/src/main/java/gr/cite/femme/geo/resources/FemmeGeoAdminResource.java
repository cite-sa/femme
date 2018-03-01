package gr.cite.femme.geo.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.geo.core.ServerGeo;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import org.geojson.GeoJsonObject;
import org.geojson.Geometry;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/admin")
public class FemmeGeoAdminResource {
	private MongoGeoDatastore geoDatastore;
	private final static ObjectMapper mapper = new ObjectMapper();
	
	@Inject
	public FemmeGeoAdminResource(MongoGeoDatastore geoDatastore) {
		this.geoDatastore = geoDatastore;
	}
	
	@POST
	@Path("servers")
	public Response insertServer(Collection server) throws DatastoreException {
		ServerGeo serverGeo = new ServerGeo();
		String id = this.geoDatastore.insert(serverGeo);
		return Response.ok(id).build();
	}

	@POST
	@Path("coverages")
	public Response insertCoverage(CoverageGeo coverage) throws DatastoreException {
		String id = this.geoDatastore.insert(coverage);
		if(id.equals("inserted")) {
            return Response.ok().build();
        }
        else{
		    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
	}
}
