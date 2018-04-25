package gr.cite.femme.semantic.search.resources;

import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.semantic.search.model.SemanticDocument;
import gr.cite.femme.semantic.search.repository.DataResponse;
import gr.cite.femme.semantic.search.repository.ElasticsearchRepository;
import gr.cite.femme.semantic.search.taxonomy.TaxonomyParser;
import gr.cite.femme.semantic.search.taxonomy.TaxonomyUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Path("")
public class SemanticSearchResource {

    private ElasticsearchRepository repository;


    @Inject
    public SemanticSearchResource(ElasticsearchRepository repository ) {
        this.repository = repository;
    }

    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataResponse<SemanticDocument> searchListWithScores(@QueryParam("term") String term){
        Map<String,Integer> termScores = new HashMap<>();
        List<String> terms = TaxonomyParser.getTermsFromTaxonomy(term);
        TaxonomyUtils.buildTermsWithWeights(terms);
        try {
            repository.getDocumentWithWeights(termScores);
        } catch (FemmeFulltextException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
