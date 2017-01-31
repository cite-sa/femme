package gr.cite.femme.metadata.xpath.application;

import gr.cite.femme.metadata.xpath.resources.MetadataXPathResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("restAPI")
public class MetadataXPathApplication extends ResourceConfig {
	
	public MetadataXPathApplication() {
		register(JacksonFeature.class);
		register(MetadataXPathResource.class);
	}
	
}
