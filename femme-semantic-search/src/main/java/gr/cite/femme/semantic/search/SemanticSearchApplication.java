package gr.cite.femme.semantic.search;

import gr.cite.femme.semantic.search.resources.SemanticSearchAdminResource;
import gr.cite.femme.semantic.search.resources.SemanticSearchResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("restAPI")
public class SemanticSearchApplication extends ResourceConfig {
    public SemanticSearchApplication() {

        register(JacksonFeature.class);
        register(SemanticSearchResource.class);
        register(SemanticSearchAdminResource.class);
    }
}
