package gr.cite.femme.fulltext.application.resources;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FulltextSearchEngine;
import gr.cite.femme.fulltext.engine.elasticsearch.FulltextSearchException;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class FulltextSearchResource {
	private FulltextSearchEngine engine;

	public FulltextSearchResource(FulltextSearchEngine engine) {
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
		} catch (FulltextSearchException e) {
			throw new WebApplicationException(e);
		}
		return Response.ok("done").build();
	}

	@GET
	@Path("elements")
	//@Consumes(MediaType.APPLICATION_JSON)
	public Response search() {
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	@DELETE
	@Path("elements/{id}")
	public Response delete(@PathParam("id") String id) {
		try {
			this.engine.delete(id);
		} catch (FulltextSearchException e) {
			throw new WebApplicationException(e);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("elements")
	public Response deleteByElementId(@QueryParam("elementId") String elementId) {
		this.engine.deleteByElementId(elementId);

		return Response.ok().build();
	}
}
