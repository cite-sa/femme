package gr.cite.femme.application;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
/*import org.glassfish.jersey.media.multipart.MultiPartFeature;*/
import org.glassfish.jersey.server.ResourceConfig;

import gr.cite.femme.application.resources.FemmeResource;

@ApplicationPath("restAPI")
public class FemmeApplication extends ResourceConfig {

	public FemmeApplication(){
		register(JacksonFeature.class);
		/*register(MultiPartFeature.class);*/
		register(FemmeResource.class);
	}
}
