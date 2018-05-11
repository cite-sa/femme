package gr.cite.femme.fulltext.application.resources;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;
import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.fulltext.engine.FulltextIndexEngine;
import gr.cite.femme.fulltext.engine.FulltextSemanticResult;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SemanticSearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class FulltextIndexResource {
	private static final Logger logger = LoggerFactory.getLogger(FulltextIndexResource.class);

	private FulltextIndexEngine engine;

	@Inject
	public FulltextIndexResource(FulltextIndexEngine engine) {
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
	public Response search(FulltextSearchQueryMessenger query) throws FemmeFulltextException, IOException, SemanticSearchException {
		if (query == null) throw new FemmeFulltextException("Query body is required", Response.Status.BAD_REQUEST.getStatusCode());
		
		List<FulltextSemanticResult> results = new ArrayList<>();
		try {
			results = this.engine.search(query);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return Response.ok(results).build();
	}

}
