package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import gr.cite.femme.fulltext.core.ExpansionQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class QueryExpander {
	private static final Logger logger = LoggerFactory.getLogger(QueryExpander.class);
	
	private ElasticsearchClient elasticsearchClient;
	
	@Inject
	public QueryExpander(ElasticsearchClient elasticsearchClient) {
		this.elasticsearchClient = elasticsearchClient;
	}
	
	SearchSourceBuilder expand(String field, String term, String type) throws SemanticSearchException {
		TaxonomyTerm taxonomyTerm = findSourceTerm(term);
		
		List<TaxonomyTerm> taxonomyTerms = Collections.singletonList(taxonomyTerm);
		
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		expandedResults.put(level, taxonomyTerms);
		
		Set<TaxonomyTerm> allTaxonomyTerms = new HashSet<>(taxonomyTerms);
		
		while (! taxonomyTerms.isEmpty()) {
			level++;
			
			if ("related".equals(type) && level > 1) break;
			
			taxonomyTerms = getNonDuplicateNextLevelTerms(taxonomyTerms, type, allTaxonomyTerms);
			
			allTaxonomyTerms.addAll(taxonomyTerms);
			if (! taxonomyTerms.isEmpty()) {
				expandedResults.put(level, taxonomyTerms);
			}
		}
		
		return buildExpansionQuery(expandedResults, field);
	}
	
	private TaxonomyTerm findSourceTerm(String term) throws SemanticSearchException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("label.keyword", term));
		
		List<TaxonomyTerm> taxonomyTerms = this.elasticsearchClient.getByQuery(searchSourceBuilder, TaxonomyTerm.class);
		
		if (taxonomyTerms == null || taxonomyTerms.size() == 0) throw new NoSuchElementException("No taxonomy term matching [" + term + "]");
		return taxonomyTerms.get(0);
	}
	
	private List<TaxonomyTerm> getNonDuplicateNextLevelTerms(List<TaxonomyTerm> taxonomyTerms, String expansionDirection, Set<TaxonomyTerm> uniqueTaxonomyTerms) {
		return taxonomyTerms.stream().map(taxonomyTerm -> retrieveNextLevelTerms(taxonomyTerm, expansionDirection)).flatMap(Collection::stream)
							.filter(distinctByKey(TaxonomyTerm::getId))
							.filter(taxonomyTerm -> !uniqueTaxonomyTerms.contains(taxonomyTerm))
							.collect(Collectors.toList());
	}
	
	private List<TaxonomyTerm> retrieveNextLevelTerms(TaxonomyTerm taxonomyTerm, String expansionDirection) {
		return getExpansionIds(expansionDirection, taxonomyTerm).stream().map(expansionId -> {
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
	
	public SearchSourceBuilder expandNew(String field, String term, ExpansionQuery expansionQuery) throws SemanticSearchException {
		TaxonomyTerm sourceTerm = findSourceTerm(term);
		
		Map<Integer, List<TaxonomyTerm>> expansionTerms;
		if ("broader".equals(expansionQuery.getDirection())) {
			expansionTerms = getBroaderTerms(sourceTerm, expansionQuery);
		} else if ("narrower".equals(expansionQuery.getDirection())) {
			expansionTerms = getNarrowerTerms(sourceTerm, expansionQuery);
		} else {
			throw new IllegalArgumentException("Expansion type must be specified. Available options are: broader, narrower, related");
		}
		
		return buildExpansionQuery(expansionTerms, field);
	}
	
	private Map<Integer, List<TaxonomyTerm>>  getNarrowerTerms(TaxonomyTerm sourceTerm, ExpansionQuery expansionQuery) {
		Set<TaxonomyTerm> allTaxonomyTerms = new HashSet<>();
		List<TaxonomyTerm> currentLevelTerms = Collections.singletonList(sourceTerm);
		allTaxonomyTerms.addAll(currentLevelTerms);
		
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		expandedResults.put(level, currentLevelTerms);
		
		
		while (! currentLevelTerms.isEmpty() && (expansionQuery.getMaxNarrower() == -1 || level < expansionQuery.getMaxNarrower())) {
			level++;
			
			currentLevelTerms = getNonDuplicateNextLevelTerms(currentLevelTerms, "narrower", allTaxonomyTerms);
			allTaxonomyTerms.addAll(currentLevelTerms);
			
			if (! currentLevelTerms.isEmpty()) {
				expandedResults.put(level, currentLevelTerms);
			}
		}
		return expandedResults;
	}
	
	private Map<Integer, List<TaxonomyTerm>> getBroaderTerms(TaxonomyTerm sourceTerm, ExpansionQuery expansionQuery) {
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		
		Set<TaxonomyTerm> uniqueTaxonomyTerms = new HashSet<>();
		uniqueTaxonomyTerms.add(sourceTerm);
		List<TaxonomyTerm> currentLevelTerms = Collections.singletonList(sourceTerm);
		
		List<TaxonomyTerm> childrenTerms = getAllChildrenTerms(currentLevelTerms, uniqueTaxonomyTerms);
		childrenTerms.addAll(currentLevelTerms);
		uniqueTaxonomyTerms.addAll(childrenTerms);
		
		expandedResults.put(level, childrenTerms);
		
		while (! currentLevelTerms.isEmpty() && (expansionQuery.getMaxBroader() == -1 || level < expansionQuery.getMaxBroader())) {
			level++;
			
			currentLevelTerms = getNonDuplicateNextLevelTerms(currentLevelTerms, "broader", uniqueTaxonomyTerms);
			uniqueTaxonomyTerms.addAll(currentLevelTerms);
			
			childrenTerms = getAllChildrenTerms(currentLevelTerms, uniqueTaxonomyTerms);
			currentLevelTerms.addAll(childrenTerms);
			uniqueTaxonomyTerms.addAll(childrenTerms);
			
			if (! currentLevelTerms.isEmpty()) {
				expandedResults.put(level, currentLevelTerms);
			}
		}
		
		return expandedResults;
	}
	
	private List<TaxonomyTerm> getAllChildrenTerms(List<TaxonomyTerm> terms, Set<TaxonomyTerm> uniqueTaxonomyTerms) {
		List<TaxonomyTerm> allChildrenTerms = new ArrayList<>();
		List<TaxonomyTerm> nextLevelTerms = terms;
		int level = 0;
		do {
			nextLevelTerms = getNonDuplicateNextLevelTerms(nextLevelTerms, "narrower", uniqueTaxonomyTerms);
			allChildrenTerms.addAll(nextLevelTerms);
			uniqueTaxonomyTerms.addAll(nextLevelTerms);
			level ++;
		} while (nextLevelTerms.size() > 0/* && level < 3*/);
		
		return allChildrenTerms;
	}
	
	private Set<TaxonomyTerm> getAllChildrenTerms(List<TaxonomyTerm> terms) {
		Set<TaxonomyTerm> allChildrenTerms = new HashSet<>();
		List<TaxonomyTerm> nextLevelTerms = terms;
		
		do {
			nextLevelTerms = getNonDuplicateNextLevelTerms(nextLevelTerms, "narrower", allChildrenTerms);
			allChildrenTerms.addAll(nextLevelTerms);
			
		} while (nextLevelTerms.size() > 0);
		
		return allChildrenTerms;
	}
	
	private List<TaxonomyTerm> getChildrenTerms(TaxonomyTerm term) {
		return null;
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
	}
	
	private SearchSourceBuilder buildExpansionQuery(Map<Integer, List<TaxonomyTerm>> expandedResults, String field) {
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		Float maxBoost = new Integer(expandedResults.size()).floatValue();
		
		expandedResults.forEach((key, value) -> {
			Float boost = (maxBoost - key);
			
			value.forEach(taxonomyTerm -> taxonomyTerm.getLabel().stream()
						  .map(label -> QueryBuilders.constantScoreQuery(QueryBuilders.termQuery(field + ".keyword", label)).boost(boost))
						  .forEach(boolQueryBuilder::should));
		});
		
		searchRequestBuilder.query(boolQueryBuilder);
		return searchRequestBuilder;
	}
}
