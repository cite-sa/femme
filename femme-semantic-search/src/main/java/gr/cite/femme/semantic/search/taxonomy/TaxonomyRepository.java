package gr.cite.femme.semantic.search.taxonomy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.semantic.search.SemanticSearchException;
import gr.cite.femme.semantic.search.config.ElasticsearchClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TaxonomyRepository {
	private static final Logger logger = LoggerFactory.getLogger(TaxonomyRepository.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private ElasticsearchClient elasticsearchClient;
	
	@Inject
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
	
	public Map<Integer, List<SkosConcept>> expand(String term, String type) throws IOException, SemanticSearchException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery("prefLabel.keyword", term));
		
		List<SkosConcept> concepts = this.elasticsearchClient.getByQuery(searchSourceBuilder, SkosConcept.class);
		concepts = Collections.singletonList(concepts.get(0));
		
		
		int level = 0;
		Map<Integer, List<SkosConcept>> expandedResults = new HashMap<>();
		expandedResults.put(level, concepts);
		
		while (! concepts.isEmpty()) {
			level++;
			
			if ("related".equals(type) && level > 1) break;
			
			concepts = concepts.stream().map(result -> getChildren(type, result)).flatMap(Collection::stream).filter(distinctByKey(SkosConcept::getUri)).collect(Collectors.toList());
			System.out.println(concepts.stream().map(c -> c.getUri().toString()).collect(Collectors.joining(", ")));
			if (! concepts.isEmpty()) {
				expandedResults.put(level, concepts);
			}
		}
		
		SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		Float maxBoost = new Integer(expandedResults.size()).floatValue();
		
		expandedResults.forEach((key, value) -> {
			Float boost = (maxBoost - key);
			value.forEach(concept -> boolQueryBuilder.should(QueryBuilders.matchQuery("prefLabel", concept.getPrefLabel().get(0)).boost(boost)));
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
		
		return expandedResults;
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
