package gr.cite.femme.index.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import gr.cite.femme.index.api.client.MetadataIndexClient;
import gr.cite.femme.core.model.Metadatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("metadata-index")
public class MetadataIndexResource {
	
	private static final Logger logger = LoggerFactory.getLogger(MetadataIndexResource.class);
	
	private MetadataIndexClient indexClient;
	
	@Inject
	public MetadataIndexResource(MetadataIndexClient indexClient) {
		this.indexClient = indexClient;
	}
	
	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping() {
		return "pong";
	}
	
	@POST
	@Path("index")
	@Consumes(MediaType.APPLICATION_JSON)
	public void index(Metadatum metadatum) {
		indexClient.index(metadatum);
	}
	
	
			
}
