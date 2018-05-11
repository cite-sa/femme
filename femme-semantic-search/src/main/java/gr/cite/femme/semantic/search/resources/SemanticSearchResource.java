package gr.cite.femme.semantic.search.resources;

import gr.cite.femme.semantic.search.SemanticSearchException;
import gr.cite.femme.semantic.search.model.SemanticDocument;
import gr.cite.femme.semantic.search.repository.DataResponse;
//import gr.cite.femme.semantic.search.repository.ElasticsearchRepository;
import gr.cite.femme.semantic.search.taxonomy.SkosConcept;
import gr.cite.femme.semantic.search.taxonomy.TaxonomyRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Path("")
public class SemanticSearchResource {
	private TaxonomyRepository taxonomyRepository;
	
	@Inject
	public SemanticSearchResource(TaxonomyRepository taxonomyRepository) {
		this.taxonomyRepository = taxonomyRepository;
	}
	
	@POST
	@Path("search")
	@Consumes(MediaType.APPLICATION_JSON)
	public DataResponse<SemanticDocument> searchListWithScores() {
		return null;
	}
	
	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("term") String term, @QueryParam("type") String type) throws IOException, SemanticSearchException {
		Map<Integer, List<SkosConcept>> expanded = this.taxonomyRepository.expand(term, type);
		return Response.ok(expanded).build();
	}
}
