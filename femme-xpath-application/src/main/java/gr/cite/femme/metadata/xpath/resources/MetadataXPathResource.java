package gr.cite.femme.metadata.xpath.resources;

import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import gr.cite.femme.metadata.xpath.core.MetadataXPathIndex;
import gr.cite.femme.model.Metadatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Component
@Path("/")
public class MetadataXPathResource {
	
	private static final Logger logger = LoggerFactory.getLogger(MetadataXPathResource.class);
	
	private MetadataXPathIndex metadataXpath;
	
	@Inject
	public MetadataXPathResource(MetadataXPathIndex metadataXpath) {
		this.metadataXpath = metadataXpath;
	}
	
	@GET
	@Path("ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping() {
		return Response.ok( "pong").build();
	}
	
	@POST
	@Path("metadata")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response index(Metadatum metadatum) {
		try {
			metadataXpath.index(metadatum);
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e1.getMessage()).build();
		} catch (UnsupportedOperationException e2) {
			logger.error(e2.getMessage(), e2);
			return Response.status(Response.Status.NOT_IMPLEMENTED).build();
		}
		return Response.status(Response.Status.CREATED).build();
	}

	@GET
	@Path("metadata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response xPath(@QueryParam("xPath") String xPath) {
		List<MaterializedPathsNode> results = metadataXpath.xPath(xPath);
		return Response.ok(results).build();
	}
	
	
			
}
