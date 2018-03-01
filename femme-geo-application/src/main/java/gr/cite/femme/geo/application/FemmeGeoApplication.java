package gr.cite.femme.geo.application;

import javax.ws.rs.ApplicationPath;

import gr.cite.femme.geo.resources.FemmeGeoAdminResource;
import gr.cite.femme.geo.resources.FemmeGeoResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("restAPI")
public class FemmeGeoApplication extends ResourceConfig {
	public FemmeGeoApplication() {
		register(JacksonFeature.class);
		register(FemmeGeoResource.class);
		register(FemmeGeoAdminResource.class);
	}
}
