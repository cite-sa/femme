package gr.cite.femme.geo.resources;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.geo.core.CoverageGeo;
import gr.cite.femme.geo.core.ServerGeo;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("admin")
public class FemmeGeoAdminResource {
	private MongoGeoDatastore geoDatastore;
	
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
	public Response insertCoverage(DataElement coverage) throws DatastoreException {
		CoverageGeo coverageGeo = new CoverageGeo();
		String id = this.geoDatastore.insert(coverageGeo);
		return Response.ok(id).build();
	}
}
