package gr.cite.femme.geo.application;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import gr.cite.femme.resources.FemmeGeoResource;

@ApplicationPath("restAPI")
public class FemmeGeoApplication extends ResourceConfig {
	public FemmeGeoApplication() {
		register(JacksonFeature.class);
		register(FemmeGeoResource.class);
	}
}
