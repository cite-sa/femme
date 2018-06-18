package gr.cite.femme.fulltext.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextField;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticFulltextIndexClient;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticResponseHit;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.MarineSpeciesTaxon;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SemanticSearchException;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SkosConcept;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.TaxonomyTerm;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.TaxonomyRepository;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
		this.mappingsConfiguration = Resources.toString(Resources.getResource("elasticsearch-mappings.json"), StandardCharsets.UTF_8);
	}

	public void insert(FulltextDocument doc) throws FemmeFulltextException, IOException {
		synchronized (this) {
			if (! this.indexClient.indexExists(this.indexName)) {
				this.indexClient.createIndex(this.indexName, this.mappingsConfiguration);
			}
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

	public List<FulltextSemanticResult> search(FulltextSearchQueryMessenger query, boolean unique) throws FemmeFulltextException, IOException, SemanticSearchException {
		if (query.getAutocompleteField() != null) {
			if (query.getExpand() != null) {
				List<TaxonomyTerm> autocompleteTaxonomyTerms = this.taxonomyRepository.autocompleteTaxonomyTerm(query.getAutocompleteField().getValue());
				
				return expandTaxonomyTerms(autocompleteTaxonomyTerms, query);
			} else {
				if (unique) {
					return toFulltextSemanticResults(aggregate(buildAutocompleteQuery(query)));
				} else {
					return toFulltextSemanticResults(search(buildAutocompleteQuery(query)));
				}
			}
		} else if (query.getMetadataField() != null) {
			if (query.getExpand() != null) {
				return toFulltextSemanticResults(search(buildExpandedQuery(query)));
			} else {
				return toFulltextSemanticResults(search(buildMatchQuery(query)));
			}
		} else {
			throw new IllegalArgumentException("No query defined");
		}
		
	}
	
	private List<FulltextSemanticResult> expandTaxonomyTerms(List<TaxonomyTerm> autocompleteTaxonomyTerms, FulltextSearchQueryMessenger query) {
		List<FulltextSemanticResult> semanticResults = new ArrayList<>();
		
		for (int i = 0; i < 3; i++) {
			if (i >= autocompleteTaxonomyTerms.size()) break;
			
			TaxonomyTerm taxonomyTerm = autocompleteTaxonomyTerms.get(i);
			FulltextSemanticResult result = new FulltextSemanticResult();
			
			FulltextDocument fulltextResult = new FulltextDocument();
			Map<String, Object> fulltextFields = new HashMap<>();
			fulltextFields.put("name", taxonomyTerm.getLabel().get(0));
			fulltextResult.setFulltextFields(fulltextFields);
			
			result.setFulltextResult(fulltextResult);
			
			FulltextField field = new FulltextField();
			field.setField("name");
			field.setValue(taxonomyTerm.getLabel().get(0));
			
			query.setMetadataField(field);
			
			try {
				List<List<FulltextSemanticResultSemantic>> resultss = searchUniqueByScore(buildExpandedQuery(query)).stream().map(res ->
					res.entrySet().stream().map(entry -> new FulltextSemanticResultSemantic(entry.getKey(), entry.getValue())).collect(Collectors.toList())
				).collect(Collectors.toList());
				
				result.setSemanticResults(resultss);
			} catch (SemanticSearchException e) {
				logger.error(e.getMessage(), e);
			}
			
			semanticResults.add(result);
		}
		
		return semanticResults;
		/*return autocompleteTaxonomyTerms.stream().map(taxonomyTerm -> {
			FulltextSemanticResult result = new FulltextSemanticResult();
			
			FulltextDocument fulltextResult = new FulltextDocument();
			Map<String, Object> fulltextFields = new HashMap<>();
			fulltextFields.put("name", taxonomyTerm.getLabel().get(0));
			fulltextResult.setFulltextFields(fulltextFields);
			
			result.setFulltextResult(fulltextResult);
			
			FulltextField field = new FulltextField();
			field.setField("name");
			field.setValue(taxonomyTerm.getLabel().get(0));
			
			query.setMetadataField(field);
			
			try {
				result.setSemanticResults(aggregate(buildExpandedQuery(query)));
			} catch (SemanticSearchException e) {
				logger.error(e.getMessage(), e);
			}
			
			return result;
			
		}).collect(Collectors.toList());*/
	}
	
	private List<Map<String, List<FulltextDocument>>> searchUniqueByScore(SearchSourceBuilder query) throws SemanticSearchException {
		Map<Float, Map<String, List<FulltextDocument>>> a = this.indexClient.searchUniqueByScore(query);
		List<Map<String, List<FulltextDocument>>> list = new ArrayList<>();
		
		List<Float> scoresByValue = a.keySet().stream().sorted((f1, f2) -> Float.compare(f2, f1)).collect(Collectors.toList());
		scoresByValue.forEach(score -> list.add(a.get(score)));
		
		return list;
		
		//List<FulltextDocument> uniques = this.indexClient.searchUniqueByScore(query);
		//List<List<FulltextDocument>>> uniqueDocs = new HashMap<>();
		
		/*return uniques.values().stream().map(scoreAndNames -> scoreAndNames.stream().map(name -> {
			FulltextDocument doc = new FulltextDocument();
			doc.setFulltextField("name", name);
			return doc;
		}).collect(Collectors.toList())).collect(Collectors.toList());*/
		
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
	
	public List<FulltextDocument> search(SearchSourceBuilder query) throws FemmeFulltextException, IOException, SemanticSearchException {
		return this.indexClient.search(query, FulltextDocument.class);
	}
	
	public List<FulltextDocument> searchUnique(SearchSourceBuilder query) throws FemmeFulltextException, IOException, SemanticSearchException {
		return this.indexClient.aggregate(query).stream().map(name -> {
			FulltextDocument doc = new FulltextDocument();
			doc.setFulltextField("name", name);
			return doc;
		}).collect(Collectors.toList());
	}
	
	private List<FulltextDocument> aggregate(SearchSourceBuilder query) throws SemanticSearchException {
		return this.indexClient.aggregate(query).stream().map(name -> {
				FulltextDocument doc = new FulltextDocument();
				doc.setFulltextField("name", name);
				return doc;
			}).collect(Collectors.toList());
	}
	

	private SearchSourceBuilder buildElasticSearchQuery(FulltextSearchQueryMessenger query) throws SemanticSearchException {
		if (query.getAutocompleteField() != null) {
			if (query.getExpand() != null) {
				return buildExpandedQuery(query);
			} else {
				return buildAutocompleteQuery(query);
			}
		} else if (query.getMetadataField() != null) {
			if (query.getExpand() != null) {
				return buildExpandedQuery(query);
			} else {
				return buildMatchQuery(query);
			}
			
		} else {
			throw new SemanticSearchException("No supported query");
		}
	}
	
	private SearchSourceBuilder buildAutocompleteQuery(FulltextSearchQueryMessenger query) {
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		MatchPhrasePrefixQueryBuilder queryBuilder = QueryBuilders.matchPhrasePrefixQuery(query.getAutocompleteField().getField(), query.getAutocompleteField().getValue());
		searchRequestBuilder.query(queryBuilder).size(1000);
		
		return searchRequestBuilder;
	}
	
	private SearchSourceBuilder buildMatchQuery(FulltextSearchQueryMessenger query) {
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(query.getMetadataField().getField() + ".keyword", query.getMetadataField().getValue());
		searchRequestBuilder.query(queryBuilder).size(1000);
		
		return searchRequestBuilder;
	}
	
	private SearchSourceBuilder buildExpandedQuery(FulltextSearchQueryMessenger query) throws SemanticSearchException {
		//return this.taxonomyRepository.buildExpansionQuery(query.getMetadataField().getField(), query.getMetadataField().getValue(), query.getExpand());
		return this.taxonomyRepository.buildExpansionQuery("name", query.getMetadataField().getValue(), query.getExpand());
	}
	
	public void storeConcepts(List<SkosConcept> concepts) {
		this.taxonomyRepository.storeConcepts(concepts);
	}
	
	public List<TaxonomyTerm> storeTaxonomyTermsTree(MarineSpeciesTaxon marineSpeciesTaxon) {
		List<TaxonomyTerm> taxonomyTerms = flattenTreeToList(marineSpeciesTaxon);
		this.taxonomyRepository.storeTaxonomyTerms(taxonomyTerms);
		return taxonomyTerms;
	}
	
	private List<TaxonomyTerm> flattenTreeToList(MarineSpeciesTaxon marineSpeciesTaxon) {
		List<TaxonomyTerm> taxa = new ArrayList<>();
		MarineSpeciesTaxon parent = null;
		
		do {
			taxa.add(transformMarineSpeciesTaxonToTaxonomyTerm(marineSpeciesTaxon, parent));
			
			parent = marineSpeciesTaxon;
			marineSpeciesTaxon = marineSpeciesTaxon.getChild();
		} while (marineSpeciesTaxon != null);
		
		return taxa;
	}
	
	private TaxonomyTerm transformMarineSpeciesTaxonToTaxonomyTerm(MarineSpeciesTaxon marineSpeciesTaxon, MarineSpeciesTaxon parent) {
		TaxonomyTerm taxonomyTerm = new TaxonomyTerm();
		
		taxonomyTerm.setId(marineSpeciesTaxon.getAphiaID());
		taxonomyTerm.setLabel(Collections.singletonList(marineSpeciesTaxon.getScientificname()));
		
		if (parent != null) {
			taxonomyTerm.setBroader(Collections.singletonList(parent.getAphiaID()));
		}
		
		if (marineSpeciesTaxon.getChild() != null) {
			taxonomyTerm.setNarrower(Collections.singletonList(marineSpeciesTaxon.getChild().getAphiaID()));
		}
		return taxonomyTerm;
		
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
