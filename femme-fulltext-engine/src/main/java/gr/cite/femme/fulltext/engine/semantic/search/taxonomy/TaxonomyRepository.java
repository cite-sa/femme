package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaxonomyRepository {
	private static final Logger logger = LoggerFactory.getLogger(TaxonomyRepository.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private ElasticsearchClient elasticsearchClient;
	
	public TaxonomyRepository(ElasticsearchClient elasticsearchClient) throws UnknownHostException {
		this.elasticsearchClient = elasticsearchClient;
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
	
	public String expand(String field, String term, String type) throws SemanticSearchException {
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
		
		/*expandedResults.forEach((key, value) -> {
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
		});*/
		
		return searchRequestBuilder.toString();
	}
	
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
	
	private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
