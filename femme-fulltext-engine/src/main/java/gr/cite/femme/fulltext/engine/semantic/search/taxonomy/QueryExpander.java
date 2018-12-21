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
		if ("all".equals(expansionQuery.getDirection())) {
			Map<Integer, List<TaxonomyTerm>> narrowerTerms = getNarrowerTerms(sourceTerm, -1);
			Map<Integer, List<TaxonomyTerm>> broaderTerms = getBroaderTerms(sourceTerm, expansionQuery.getMaxBroader());
			
			expansionTerms = mergeBroaderAndNarrower(narrowerTerms, broaderTerms);
		} else if ("broader".equals(expansionQuery.getDirection())) {
			expansionTerms = getBroaderTerms(sourceTerm, expansionQuery.getMaxBroader());
		} else if ("narrower".equals(expansionQuery.getDirection())) {
			expansionTerms = getNarrowerTerms(sourceTerm, -1);
		} else {
			throw new IllegalArgumentException("Expansion type must be specified. Available options are: broader, narrower, related");
		}
		
		return buildExpansionQuery(expansionTerms, field);
	}
	
	private Map<Integer, List<TaxonomyTerm>> mergeBroaderAndNarrower(Map<Integer, List<TaxonomyTerm>> narrowerTerms, Map<Integer, List<TaxonomyTerm>> broaderTerms) {
		Map<Integer, List<TaxonomyTerm>> terms = new HashMap<>();
		
		//terms.putAll(broaderTerms);
		for (Map.Entry<Integer, List<TaxonomyTerm>> entry: broaderTerms.entrySet()) {
			initOrAddList(entry, terms);
		}
		for (Map.Entry<Integer, List<TaxonomyTerm>> entry: narrowerTerms.entrySet()) {
			initOrAddList(entry, terms);
		}
		
		return terms;
	}
	
	private void initOrAddList(Map.Entry<Integer, List<TaxonomyTerm>> entry, Map<Integer, List<TaxonomyTerm>> terms) {
		if (terms.containsKey(entry.getKey())) {
			Set<TaxonomyTerm> temp1 = new HashSet<>(terms.get(entry.getKey()));
			Set<TaxonomyTerm> temp2 = new HashSet<>(entry.getValue());
			temp1.addAll(temp2);
			
			terms.put(entry.getKey(), new ArrayList<>(temp1));
		} else {
			terms.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
	}
	
	private Map<Integer, List<TaxonomyTerm>>  getNarrowerTerms(TaxonomyTerm sourceTerm, Integer maxLevel) {
		Set<TaxonomyTerm> allTaxonomyTerms = new HashSet<>();
		List<TaxonomyTerm> currentLevelTerms = Collections.singletonList(sourceTerm);
		allTaxonomyTerms.addAll(currentLevelTerms);
		
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		expandedResults.put(level, currentLevelTerms);
		
		
		while (! currentLevelTerms.isEmpty() && (maxLevel == -1 || level < maxLevel)) {
			level++;
			
			currentLevelTerms = getNonDuplicateNextLevelTerms(currentLevelTerms, "narrower", allTaxonomyTerms);
			allTaxonomyTerms.addAll(currentLevelTerms);
			
			if (! currentLevelTerms.isEmpty()) {
				expandedResults.put(level, currentLevelTerms);
			}
		}
		return expandedResults;
	}
	
	private Map<Integer, List<TaxonomyTerm>> getBroaderTerms(TaxonomyTerm sourceTerm, Integer maxLevel) {
		int level = 0;
		Map<Integer, List<TaxonomyTerm>> expandedResults = new HashMap<>();
		
		Set<TaxonomyTerm> uniqueTaxonomyTerms = new HashSet<>();
		uniqueTaxonomyTerms.add(sourceTerm);
		List<TaxonomyTerm> currentLevelTerms = Collections.singletonList(sourceTerm);
		
		/*List<TaxonomyTerm> childrenTerms = getAllChildrenTerms(currentLevelTerms, uniqueTaxonomyTerms);
		childrenTerms.addAll(currentLevelTerms);
		uniqueTaxonomyTerms.addAll(childrenTerms);*/
		
		/*List<TaxonomyTerm> childrenTerms = new ArrayList<>();
		childrenTerms.addAll(currentLevelTerms);
		uniqueTaxonomyTerms.addAll(childrenTerms);
		expandedResults.put(level, childrenTerms);*/
		
		while (! currentLevelTerms.isEmpty() && (maxLevel == -1 || level < maxLevel)) {
			level++;
			
			currentLevelTerms = getNonDuplicateNextLevelTerms(currentLevelTerms, "broader", uniqueTaxonomyTerms);
			uniqueTaxonomyTerms.addAll(currentLevelTerms);
			
			/*childrenTerms = getAllChildrenTerms(currentLevelTerms, uniqueTaxonomyTerms);
			currentLevelTerms.addAll(childrenTerms);
			uniqueTaxonomyTerms.addAll(childrenTerms);*/
			
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
			case "all":
				return taxonomyTerm.getBroader();
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
		
		int max = 1023;
		int total = 0;
		for (Map.Entry<Integer, List<TaxonomyTerm>> expandedResult: expandedResults.entrySet()) {
			if (total == max) break;
			
			Float boost = (maxBoost - expandedResult.getKey());
			
			for (TaxonomyTerm taxonomyTerm: expandedResult.getValue()) {
				if (total == max) break;
				
				taxonomyTerm.getLabel().stream()
					.map(label -> QueryBuilders.constantScoreQuery(QueryBuilders.termQuery(field + ".keyword", label)).boost(boost))
					.forEach(boolQueryBuilder::should);
				
				total++;
			}
		}
		
		searchRequestBuilder.query(boolQueryBuilder);
		return searchRequestBuilder;
	}
}
