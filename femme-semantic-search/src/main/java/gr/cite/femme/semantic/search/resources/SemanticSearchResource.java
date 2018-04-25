package gr.cite.femme.semantic.search.resources;

import gr.cite.femme.semantic.search.model.SemanticDocument;
import gr.cite.femme.semantic.search.repository.DataResponse;
import gr.cite.femme.semantic.search.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Component
@Path("")
public class SemanticSearchResource {

    private ElasticsearchRepository repository;

    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataResponse<SemanticDocument> searchListWithScores(){
        return null;
    }
}
