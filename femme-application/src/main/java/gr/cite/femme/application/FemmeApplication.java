package gr.cite.femme.application;

import javax.ws.rs.ApplicationPath;

import gr.cite.femme.application.resources.FemmeAdminResource;
import gr.cite.femme.application.resources.FemmeImportResource;
import gr.cite.femme.application.resources.FemmeResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("restAPI")
public class FemmeApplication extends ResourceConfig {

	public FemmeApplication() {
		register(JacksonFeature.class);
		/*register(MultiPartFeature.class);*/
		register(FemmeImportResource.class);
		register(FemmeAdminResource.class);
		register(FemmeResource.class);
	}
}
