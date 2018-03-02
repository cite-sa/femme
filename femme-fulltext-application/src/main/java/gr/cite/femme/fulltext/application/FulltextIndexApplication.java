package gr.cite.femme.fulltext.application;

import gr.cite.femme.fulltext.application.resources.FulltextIndexAdminResource;
import gr.cite.femme.fulltext.application.resources.FulltextIndexResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("restAPI")
public class FulltextIndexApplication extends ResourceConfig {
	public FulltextIndexApplication() {
		register(JacksonFeature.class);
		register(FulltextIndexResource.class);
		register(FulltextIndexAdminResource.class);
	}
}
