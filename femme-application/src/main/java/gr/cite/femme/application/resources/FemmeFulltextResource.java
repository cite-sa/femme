package gr.cite.femme.application.resources;

import gr.cite.femme.fulltext.client.FulltextIndexClientAPI;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("fulltext")
public class FemmeFulltextResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeFulltextResource.class);

	private FulltextIndexClientAPI client;

	@Inject
	public FemmeFulltextResource(FulltextIndexClientAPI client) {
		this.client = client;
	}

	@Path("elements")
	public Response searchElements(FulltextSearchQueryMessenger query) {
		List<FulltextDocument> results = this.client.search(query);
		return Response.ok(results).build();
	}

	@Path("collections")
	public Response searchCollections() {
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	@Path("dataElements")
	public Response searchDataElements() {
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

}
