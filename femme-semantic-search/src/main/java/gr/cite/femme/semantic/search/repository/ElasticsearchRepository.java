package gr.cite.femme.semantic.search.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.fulltext.engine.FulltextIndexEngine;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticFulltextIndexClient;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticResponseHit;
import gr.cite.femme.semantic.search.config.ElasticsearchClient;
import gr.cite.femme.semantic.search.model.SemanticDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ElasticsearchRepository {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String INDEX_NAME ="fulltext_search";

    private ElasticsearchClient indexClient;
    private String indexName;

    public ElasticsearchRepository(String host, int port) throws UnknownHostException {
        this(host, port, ElasticsearchRepository.INDEX_NAME);
    }

    public ElasticsearchRepository(String host, int port, String indexName) throws UnknownHostException {
        this.indexClient = new ElasticsearchClient(host, port);
        this.indexName = indexName;
    }

    public void insert(SemanticDocument doc) throws FemmeFulltextException {
    }

    public List<FulltextDocument> getDocumentWithWeights(Map<String,Integer> terms) throws FemmeFulltextException, IOException {
        ElasticQueryBuilder queryBuilder = new ElasticQueryBuilder();
        String query = queryBuilder.setTermsWithWeight(terms).build();
        System.out.println("the query is:"+query);
        return  indexClient.getByQuery(query).stream().map(ElasticResponseHit::getSource).collect(Collectors.toList());
        //return null;
    }


}
