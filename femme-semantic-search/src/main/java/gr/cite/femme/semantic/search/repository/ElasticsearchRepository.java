/*
package gr.cite.femme.semantic.search.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.semantic.search.SemanticSearchException;
import gr.cite.femme.semantic.search.config.ElasticsearchClient;
import gr.cite.femme.semantic.search.model.SemanticDocument;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class ElasticsearchRepository {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String INDEX_NAME = "semantic_search";
	
	private ElasticsearchClient indexClient;
	private String indexName;
	
	public ElasticsearchRepository(String host, int port) throws UnknownHostException {
		this(host, port, ElasticsearchRepository.INDEX_NAME);
	}
	
	*/
/*public ElasticsearchRepository(ElasticsearchClient indexClient) throws UnknownHostException {
		this.indexClient = indexClient;
	}*//*

	
	public ElasticsearchRepository(String host, int port, String indexName) throws UnknownHostException {
		this.indexClient = new ElasticsearchClient(host, port);
		this.indexName = indexName;
	}
	
	public void insert(SemanticDocument doc) throws SemanticSearchException {
	}
	
	*/
/*public List<FulltextDocument> getDocumentWithWeights(Map<String, Integer> terms) throws SemanticSearchException, IOException {
		ElasticQueryBuilder queryBuilder = new ElasticQueryBuilder();
		String query = queryBuilder.setTermsWithWeight(terms).build();
		System.out.println("the query is:" + query);
		return indexClient.getByQuery(query).stream().map(ElasticResponseHit::getSource).collect(Collectors.toList());
		//return null;
	}*//*

	
	
}
*/
