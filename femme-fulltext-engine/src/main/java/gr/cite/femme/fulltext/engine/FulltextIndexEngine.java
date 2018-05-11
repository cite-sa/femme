package gr.cite.femme.fulltext.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextField;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticFulltextIndexClient;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticResponseHit;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SemanticSearchException;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SkosConcept;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.TaxonomyRepository;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FulltextIndexEngine {
	private static final Logger logger = LoggerFactory.getLogger(FulltextIndexEngine.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String INDEX_NAME = "fulltext_search";
	
	private TaxonomyRepository taxonomyRepository;
	private ElasticFulltextIndexClient indexClient;
	private String indexName;
	private String mappingsConfiguration;

	public FulltextIndexEngine(String host, int port, TaxonomyRepository taxonomyRepository) throws IOException {
		this(host, port, FulltextIndexEngine.INDEX_NAME, taxonomyRepository);
	}

	public FulltextIndexEngine(String host, int port, String indexName, TaxonomyRepository taxonomyRepository) throws IOException {
		this.indexClient = new ElasticFulltextIndexClient(host, port);
		this.indexName = indexName;
		this.taxonomyRepository = taxonomyRepository;
		this.mappingsConfiguration = Resources.toString(Resources.getResource("elasticsearch-mappings-config.json"), StandardCharsets.UTF_8);
	}

	public void insert(FulltextDocument doc) throws FemmeFulltextException, IOException {
		if (!this.indexClient.indexExists(this.indexName)) {
			/*String settings =  "\"settings\": {" +
					"\"analysis\": {" +
						"\"filter\": {" +
							"\"grams_filter\": {" +
								"\"type\":\"ngram\"," +
								"\"min_gram\": 2," +
								"\"max_gram\": 3" +
							"}" +
						"}," +
					"\"analyzer\": {" +
						"\"grams\": {" +
							"\"type\":\"custom\"," +
							"\"tokenizer\": \"standard\"," +
							"\"filter\": [" +
								"\"lowercase\"," +
								"\"grams_filter\"" +
							"]" +
						"}" +
					"}" +
				"}" +
			"}";*/
			
			
			/*String mappings = "\"mappings\" : {" +
					"\"elements\" : {" +
						"\"dynamic_templates\": [" +
							"{" +
								"\"strings\": {" +
									"\"match_mapping_type\": \"string\"," +
									"\"mapping\": {" +
										"\"type\": \"text\"," +
										"\"copy_to\": \"all_fields\"" +
									"}" +
								"}" +
							"}" +
						"]," +
						"\"properties\": {" +
							*//*"\"name\": {" +
								"\"type\": \"text\"," +
								"\"analyzer\": \"simple\"" +
							"}," +*//*
							"\"elementId\": {" +
								"\"type\": \"keyword\"," +
								"\"index\": true" +
							"}," +
							"\"metadatumId\": {" +
								"\"type\": \"keyword\"," +
								"\"index\": true" +
							"}" +
						"}" +
					"}" +
				"}";*/


			this.indexClient.createIndex(this.indexName, this.mappingsConfiguration);
		}

		String jsonDoc;
		try {
			jsonDoc = mapper.writeValueAsString(doc);
		} catch (JsonProcessingException e) {
			throw new FemmeFulltextException(e.getMessage(), e);
		}

		this.indexClient.insert(jsonDoc, this.indexName);
	}

	public void delete(String id) throws FemmeFulltextException {
		this.indexClient.delete(id, this.indexName);
	}

	public void deleteByElementId(String elementId) throws FemmeFulltextException {
		deleteByQuery("elementId", elementId);
	}

	public void deleteByMetadatumId(String metadatumId) throws FemmeFulltextException {
		deleteByQuery("metadatumId", metadatumId);
	}

	private void deleteByQuery(String field, String value) throws FemmeFulltextException {
		String deleteQuery = "{" +
				"\"query\":{" +
					"\"constant_score\":{" +
						"\"filter\":{" +
							"\"term\":{" +
								"\"" + field + "\":\"" + value + "\"" +
							"}" +
						"}" +
					"}" +
				"}" +
			"}";
		this.indexClient.deleteByQuery(deleteQuery, this.indexName);
	}

	public List<FulltextSemanticResult> search(FulltextSearchQueryMessenger query) throws FemmeFulltextException, IOException, SemanticSearchException {
		if (query.getAutocompleteField() != null) {
			if (! Strings.isNullOrEmpty(query.getExpand())) {
				
				List<FulltextDocument> autocompleteResults = search(buildAutocompleteQuery(query));
				
				return autocompleteResults.stream().map(autocompleteResult -> {
					FulltextSemanticResult result = new FulltextSemanticResult();
					result.setFulltextResult(autocompleteResult);
					
					FulltextField field = new FulltextField();
					field.setField(query.getAutocompleteField().getField());
					field.setValue((String) autocompleteResult.getFulltextField(query.getAutocompleteField().getField()));
					
					query.setMetadataField(field);
					
					try {
						result.setSemanticResults(search(buildExpandQuery(query)));
					} catch (FemmeFulltextException | IOException | SemanticSearchException e) {
						logger.error(e.getMessage(), e);
					}
					
					return result;
					
				}).collect(Collectors.toList());
			} else {
				return toFulltextSemanticResults(search(buildAutocompleteQuery(query)));
			}
		} else if (query.getMetadataField() != null) {
			if (! Strings.isNullOrEmpty(query.getExpand())) {
				return toFulltextSemanticResults(search(buildExpandQuery(query)));
			} else {
				return toFulltextSemanticResults(search(buildMatchQuery(query)));
			}
		} else {
			throw new IllegalArgumentException("No query defined");
		}
		
	}
	
	private List<FulltextSemanticResult> toFulltextSemanticResults(List<FulltextDocument> fulltextDocuments) {
		return fulltextDocuments.stream().map(FulltextIndexEngine::toFulltextSemanticResult).collect(Collectors.toList());
	}
	
	private static FulltextSemanticResult toFulltextSemanticResult(FulltextDocument fulltextDocument) {
		FulltextSemanticResult result = new FulltextSemanticResult();
		result.setFulltextResult(fulltextDocument);
		return result;
	}
	
	public List<FulltextDocument> search(String query) throws FemmeFulltextException, IOException, SemanticSearchException {
		return this.indexClient.search(query, this.indexName).stream().map(ElasticResponseHit::getSource).collect(Collectors.toList());
	}

	private String buildElasticSearchQuery(FulltextSearchQueryMessenger query) throws IOException, SemanticSearchException {
		if (query.getAutocompleteField() != null) {
			/*return "{" +
				"\"query\": {" +
					"\"match\" : {" +
						"\"" + query.getMetadataField().getField() + "\" : {" +
							"\"query\" : \"" + query.getMetadataField().getValue() + "\"," +
							"\"fuzziness\": \"auto\"" +
						"}" +
					"}" +
				"}" +
			"}";*/
			/*return "{" +
				"\"query\": {" +
					"\"match_phrase_prefix\": {" +
						"\"" + query.getMetadataField().getField() + "\": \"" + query.getMetadataField().getValue() + "\"" +
					"}" +
				"}" +
			"}";*/
			if (! Strings.isNullOrEmpty(query.getExpand())) {
				return buildExpandQuery(query);
			} else {
				return buildAutocompleteQuery(query);
			}
		} else if (query.getMetadataField() != null) {
			if (! Strings.isNullOrEmpty(query.getExpand())) {
				return buildExpandQuery(query);
			} else {
				return buildMatchQuery(query);
			}
			
		}
		return "";
	}
	
	private String buildAutocompleteQuery(FulltextSearchQueryMessenger query) {
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		MatchPhrasePrefixQueryBuilder queryBuilder = QueryBuilders.matchPhrasePrefixQuery(query.getAutocompleteField().getField(), query.getAutocompleteField().getValue());
		searchRequestBuilder.query(queryBuilder);
		
		return searchRequestBuilder.toString();
	}
	
	private String buildMatchQuery(FulltextSearchQueryMessenger query) {
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(query.getMetadataField().getField(), query.getMetadataField().getValue());
		searchRequestBuilder.query(queryBuilder);
		
		return searchRequestBuilder.toString();
	}
	
	private String buildExpandQuery(FulltextSearchQueryMessenger query) throws SemanticSearchException {
		return this.taxonomyRepository.expand(query.getMetadataField().getField(), query.getMetadataField().getValue(), query.getExpand());
	}
	
	public void storeConcepts(List<SkosConcept> concepts) {
		this.taxonomyRepository.storeConcepts(concepts);
	}

	public static void main(String[] args) {
		/*String settings =  "\"settings\": {" +
				"\"analysis\": {" +
				"\"filter\": {" +
				"\"grams_filter\": {" +
				"\"type\":\"ngram\"," +
				"\"min_gram\": 2," +
				"\"max_gram\": 3" +
				"}" +
				"}," +
				"\"analyzer\": {" +
				"\"grams\": {" +
				"\"type\":\"custom\"," +
				"\"tokenizer\": \"standard\"," +
				"\"filter\": [" +
				"\"lowercase\"," +
				"\"grams_filter\"" +
				"]" +
				"}" +
				"}" +
				"}" +
				"}";

		String mappings = "\"mappings\" : {" +
				"\"elements\" : {" +
				"\"properties\" : {" +
				"\"name\": {" +
				"\"type\": \"text\"," +
				"\"analyzer\": \"simple\"" +
				"}," +
				"\"elementId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}," +
				"\"metadatumId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}" +
				"}" +
				"}" +
				"}";*/


		String settings =  "\"settings\": {" +
				"\"analysis\": {" +
				"\"filter\": {" +
				"\"grams_filter\": {" +
				"\"type\":\"ngram\"," +
				"\"min_gram\": 2," +
				"\"max_gram\": 7" +
				"}" +
				"}," +
				"\"analyzer\": {" +
				"\"grams\": {" +
				"\"type\":\"custom\"," +
				"\"tokenizer\": \"standard\"," +
				"\"filter\": [" +
				"\"lowercase\"," +
				"\"grams_filter\"" +
				"]" +
				"}" +
				"}" +
				"}" +
				"}";

		String mappings = "\"mappings\" : {" +
				"\"elements\" : {" +
				"\"properties\" : {" +
				"\"name\": {" +
				"\"type\": \"string\"," +
				"\"analyzer\": \"grams\"" +
				"}," +
				"\"elementId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}," +
				"\"metadatumId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}" +
				"}" +
				"}" +
				"}";

		System.out.println("{" + settings + "," + mappings + "}");
	}
}
