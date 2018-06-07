package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.ExpansionQuery;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class TaxonomyRepository {
	private static final Logger logger = LoggerFactory.getLogger(TaxonomyRepository.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private ElasticsearchClient elasticsearchClient;
	private QueryExpander queryExpander;
	
	@Inject
	public TaxonomyRepository(ElasticsearchClient elasticsearchClient, QueryExpander queryExpander) {
		this.elasticsearchClient = elasticsearchClient;
		this.queryExpander = queryExpander;
	}
	
	public void storeConcepts(List<SkosConcept> concepts) {
		List<Map<String, Object>> serializedConcepts = concepts.stream().map(TaxonomyRepository::convertSkosConceptToMap).collect(Collectors.toList());
		
		try {
			this.elasticsearchClient.insert(serializedConcepts);
		} catch (SemanticSearchException e) {
			logger.error("Error inserting concepts");
		}
	}
	
	private static Map<String, Object> convertSkosConceptToMap(SkosConcept concept) {
		return mapper.convertValue(concept, new TypeReference<Map<String, Object>>(){});
	}
	
	/*public String buildExpansionQuery(String field, String term, String type) throws SemanticSearchException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("prefLabel.keyword", term));
		
		List<SkosConcept> concepts = this.elasticsearchClient.getByQuery(searchSourceBuilder, SkosConcept.class);
		concepts = Collections.singletonList(concepts.get(0));
		
		int level = 0;
		Map<Integer, List<SkosConcept>> expandedResults = new HashMap<>();
		expandedResults.put(level, concepts);
		
		Set<SkosConcept> allConcepts = new HashSet<>(concepts);
		
		while (! concepts.isEmpty()) {
			level++;
			
			if ("related".equals(type) && level > 1) break;
			
			concepts = concepts.stream().map(result -> getChildren(type, result)).flatMap(Collection::stream)
						   .filter(distinctByKey(SkosConcept::getUri))
						   .filter(concept -> !allConcepts.contains(concept))
						   .collect(Collectors.toList());
			
			allConcepts.addAll(concepts);
			
			if (! concepts.isEmpty()) {
				expandedResults.put(level, concepts);
			}
		}
		
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		Float maxBoost = new Integer(expandedResults.size()).floatValue();
		
		expandedResults.forEach((key, value) -> {
			Float boost = (maxBoost - key);
		
			value.forEach(concept ->
				concept.getPrefLabel().stream().map(prefLabel -> QueryBuilders.constantScoreQuery(
						QueryBuilders.termQuery(field + ".keyword", prefLabel)
				).boost(boost)).forEach(boolQueryBuilder::should));
			});
		
		searchRequestBuilder.query(boolQueryBuilder);
		System.out.println(searchRequestBuilder.toString());
		
		*//*expandedResults.forEach((key, value) -> {
			String dashes = "";
			for (int i = 0; i < key; i++) {
				dashes += "--";
				if (i == key - 1) dashes += " ";
			}
			final String theDashes = dashes;
			value.forEach(conv -> {
				System.out.print(theDashes);
				System.out.print(conv.getUri() + " - " + conv.getPrefLabel().stream().collect(Collectors.joining(", ")));
				System.out.print("\n");
			});
		});*//*
		
		return searchRequestBuilder.toString();
	}*/
	
	private List<SkosConcept> getChildren(String type, SkosConcept concept) {
		return getExpansionUris(type, concept).stream().map(expansionUri -> {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(QueryBuilders.termQuery("uri.keyword", expansionUri.toString()));
			
			try {
				return this.elasticsearchClient.getByQuery(searchSourceBuilder, SkosConcept.class);
			} catch (SemanticSearchException e) {
				logger.error(e.getMessage(), e);
			}
			
			return null;
			
		}).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	private List<URI> getExpansionUris(String type, SkosConcept concept) {
		switch (type) {
			case "broader":
				return concept.getBroader();
			case "narrower":
				return concept.getNarrower();
			case "related":
				return concept.getRelated();
			default:
				throw new IllegalArgumentException("Expansion type must be specified. Available options are: broader, narrower, related");
		}
	}
	
	synchronized public void storeTaxonomyTerms(List<TaxonomyTerm> taxonomyTerms) {
		String uuid = UUID.randomUUID().toString();
		logger.debug("STORING - " + uuid);
		for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
			logger.debug(uuid + " - NEW - " + taxonomyTerm.getLabel().get(0));
			
			 SearchHit existingTaxon = searchTaxonById(taxonomyTerm.getId());
			
			if (existingTaxon == null) {
				try {
					this.elasticsearchClient.insert(convertTaxonomyTermToMap(taxonomyTerm));
				} catch (SemanticSearchException e) {
					logger.error("Error inserting taxonomy terms");
				}
			} else {
				String id = existingTaxon.getId();
				TaxonomyTerm existing;
				try {
					existing = mapper.readValue(existingTaxon.getSourceAsString(), TaxonomyTerm.class);
					
					logger.debug(uuid + " - EXISTING - " + existing.getLabel().get(0));
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					continue;
				}
				taxonomyTerm = mergeNewAndExistingTaxon(existing, taxonomyTerm);
				
				try {
					this.elasticsearchClient.update(id, convertTaxonomyTermToMap(taxonomyTerm));
				} catch (SemanticSearchException e) {
					logger.error("Error updating taxonomy terms");
				}
			}
		}
	}
	
	private TaxonomyTerm mergeNewAndExistingTaxon(TaxonomyTerm existingTaxonomyTerm, TaxonomyTerm newTaxonomyTerm) {
		Set<String> broader = new HashSet<>();
		broader.addAll(existingTaxonomyTerm.getBroader());
		broader.addAll(newTaxonomyTerm.getBroader());
		
		Set<String> narrower = new HashSet<>();
		narrower.addAll(existingTaxonomyTerm.getNarrower());
		narrower.addAll(newTaxonomyTerm.getNarrower());
		
		newTaxonomyTerm.setBroader(new ArrayList<>(broader));
		newTaxonomyTerm.setNarrower(new ArrayList<>(narrower));
		
		return newTaxonomyTerm;
	}
	
	private boolean existsTaxon(String id) {
		if (id == null || "".equals(id)) throw new IllegalArgumentException("id must have a value");
		return retrieveTaxonomyTermById(id) != null;
	}
	
	private SearchHit searchTaxonById(String id) {
		if (id == null || "".equals(id)) throw new IllegalArgumentException("id must have a value");
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", id));
		
		try {
			List<SearchHit> results = this.elasticsearchClient.searchByQuery(searchSourceBuilder);
			if (results != null && results.size() > 0) {
				return results.get(0);
			} else {
				return null;
			}
		} catch (SemanticSearchException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	private TaxonomyTerm retrieveTaxonomyTermById(String id) {
		if (id == null || "".equals(id)) throw new IllegalArgumentException("id must have a value");
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", id));
		
		try {
			List<TaxonomyTerm> taxa = this.elasticsearchClient.getByQuery(searchSourceBuilder, TaxonomyTerm.class);
			if (taxa!= null && taxa.size() == 1) {
				return taxa.get(0);
			} else {
				return null;
			}
		} catch (SemanticSearchException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	private static Map<String, Object> convertTaxonomyTermToMap(TaxonomyTerm taxonomyTerm) {
		return mapper.convertValue(taxonomyTerm, new TypeReference<Map<String, Object>>(){});
	}
	
	public List<TaxonomyTerm> autocompleteTaxonomyTerm(String term) throws SemanticSearchException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchPhrasePrefixQuery("label", term));
		return this.elasticsearchClient.getByQuery(searchSourceBuilder, TaxonomyTerm.class);
	}
	
	public SearchSourceBuilder buildExpansionQuery(String field, String term, ExpansionQuery expansionQuery) throws SemanticSearchException {
		return this.queryExpander.expandNew(field, term, expansionQuery);
	}
	
	/*public String buildExpansionQuery(String field, String term, String type) throws SemanticSearchException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("label.keyword", term));
		
		List<TaxonomyTerm> taxa = this.elasticsearchClient.getByQuery(searchSourceBuilder, TaxonomyTerm.class);
		taxa = Collections.singletonList(taxa.get(0));
		
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		expandedResults.put(level, taxa);
		
		Set<TaxonomyTerm> allTaxa = new HashSet<>(taxa);
		
		while (! taxa.isEmpty()) {
			level++;
			
			if ("related".equals(type) && level > 1) break;
			
			taxa = taxa.stream().map(result -> getChildren(type, result)).flatMap(Collection::stream)
						   .filter(distinctByKey(TaxonomyTerm::getId))
						   .filter(taxonomyTerm -> !allTaxa.contains(taxonomyTerm))
						   .collect(Collectors.toList());
			
			allTaxa.addAll(taxa);
			
			if (! taxa.isEmpty()) {
				expandedResults.put(level, taxa);
			}
		}
		
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		Float maxBoost = new Integer(expandedResults.size()).floatValue();
		
		expandedResults.forEach((key, value) -> {
			Float boost = (maxBoost - key);
			
			value.forEach(taxonomyTerm ->
				  taxonomyTerm.getLabel().stream().map(label -> QueryBuilders.constantScoreQuery(
					  QueryBuilders.termQuery(field + ".keyword", label)
				  ).boost(boost)).forEach(boolQueryBuilder::should));
		});
		
		searchRequestBuilder.query(boolQueryBuilder);
		System.out.println(searchRequestBuilder.toString());
		
		return searchRequestBuilder.toString();
	}
	
	private List<TaxonomyTerm> getChildren(String type, TaxonomyTerm taxonomyTerm) {
		return getExpansionIds(type, taxonomyTerm).stream().map(expansionId -> {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(QueryBuilders.termQuery("id.keyword", expansionId));
			
			try {
				return this.elasticsearchClient.getByQuery(searchSourceBuilder, TaxonomyTerm.class);
			} catch (SemanticSearchException e) {
				logger.error(e.getMessage(), e);
			}
			
			return null;
			
		}).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	private List<String> getExpansionIds(String type, TaxonomyTerm taxonomyTerm) {
		switch (type) {
			case "broader":
				return taxonomyTerm.getBroader();
			case "narrower":
				return taxonomyTerm.getNarrower();
			case "related":
				return taxonomyTerm.getRelated();
			default:
				throw new IllegalArgumentException("Expansion type must be specified. Available options are: broader, narrower, related");
		}
	}
	
	private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}*/
}
