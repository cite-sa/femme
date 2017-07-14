package gr.cite.femme.fulltext.application.resources;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FulltextIndexEngine;
import gr.cite.femme.fulltext.engine.FulltextIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("admin")
public class FulltextIndexAdminResource {
	private static final Logger logger = LoggerFactory.getLogger(FulltextIndexAdminResource.class);

	private FulltextIndexEngine engine;

	@Inject
	public FulltextIndexAdminResource(FulltextIndexEngine engine) {
		this.engine = engine;
	}

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("pong").build();
	}

	@POST
	@Path("elements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(FulltextDocument doc) {
		try {
			this.engine.insert(doc);
		} catch (FulltextIndexException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		return Response.ok("done").build();
	}

	@DELETE
	@Path("elements/{id}")
	public Response delete(@PathParam("id") String id) {
		try {
			this.engine.delete(id);
		} catch (FulltextIndexException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("elements")
	public Response deleteByElementId(@QueryParam("elementId") String elementId, @QueryParam("metadatumId") String metadatumId) {
		if (elementId == null && metadatumId == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No elementId or metadatumId specified").build();
		}

		try {
			if (metadatumId != null) {
				this.engine.deleteByMetadatumId(metadatumId);
			} else {
				this.engine.deleteByElementId(elementId);
			}
		} catch (FulltextIndexException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}

		return Response.ok().build();
	}
}
