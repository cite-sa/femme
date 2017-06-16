package gr.cite.femme.fulltext.application;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("restAPI")
public class FulltextSearchApplication extends ResourceConfig {
	public FulltextSearchApplication() {
		register(JacksonFeature.class);
	}
}
