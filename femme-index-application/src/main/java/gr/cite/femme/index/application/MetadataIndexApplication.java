package gr.cite.femme.index.application;

import javax.ws.rs.ApplicationPath;

import gr.cite.femme.index.resources.MetadataIndexResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("restAPI")
public class MetadataIndexApplication extends ResourceConfig {
	
	public MetadataIndexApplication() {
		register(JacksonFeature.class);
		register(MetadataIndexResource.class);
	}
	
}
