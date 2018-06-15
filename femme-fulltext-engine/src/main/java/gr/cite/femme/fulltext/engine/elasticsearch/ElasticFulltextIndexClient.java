package gr.cite.femme.fulltext.engine.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.SemanticSearchException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.xerces.xs.StringList;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ElasticFulltextIndexClient {
	private static final Logger logger = LoggerFactory.getLogger(ElasticFulltextIndexClient.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String ELASTICSEARCH_HOST_NAME = "localhost";
	private static final int ELASTICSEARCH_PORT = 9200;
	private static final String ELASTICSEARCH_INDEX_ALIAS = "fulltext_search";
	private static final String ELASTICSEARCH_TYPE = "elements";

	private RestClient client;
	private RestHighLevelClient highLevelClient;
	
	private String indexAlias;
	private AtomicBoolean indexAliasCreated;

	public ElasticFulltextIndexClient() throws UnknownHostException {
		this(ElasticFulltextIndexClient.ELASTICSEARCH_HOST_NAME,
				ElasticFulltextIndexClient.ELASTICSEARCH_PORT,
				ElasticFulltextIndexClient.ELASTICSEARCH_INDEX_ALIAS);
	}

	public ElasticFulltextIndexClient(String hostName, int port) {
		this(hostName, port, ElasticFulltextIndexClient.ELASTICSEARCH_INDEX_ALIAS);
	}

	public ElasticFulltextIndexClient(String hostName, int port, String indexAlias) {
		this.indexAlias = indexAlias;
		this.client = RestClient.builder(new HttpHost(hostName, port, "http")).build();
		this.highLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(hostName, port, "http")));
	}

	boolean isIndexAliasCreated() {
		return this.indexAliasCreated.get();
	}

	public void close() throws IOException {
		client.close();
	}

	public RestClient get() {
		return client;
	}

	public String getIndexAlias() {
		return indexAlias;
	}

	public String getIndexPrefix(String metadataSchemaId) {
		return this.indexAlias + metadataSchemaId;
	}

	public String getFullIndexName(String metadataSchemaId, String uniqueId) {
		return this.indexAlias + metadataSchemaId + "_" + uniqueId;
	}

	public String createIndex() throws IOException {
		String indexName = this.indexAlias + "_" + Instant.now().toEpochMilli();
		this.client.performRequest("PUT", "/" + indexName);
		return indexName;
	}

	public void createIndex(String indexName) throws FemmeFulltextException {
		try {
			this.client.performRequest("PUT", "/" + indexName);
		} catch (IOException e) {
			throw new FemmeFulltextException("Index " + indexName + " creation failed", e);
		}
	}

	public void createIndex(String indexName, String mapping) throws FemmeFulltextException {
		//CreateIndexRequest request = new CreateIndexRequest(indexName);
		try {
			this.client.performRequest("PUT", "/" + indexName, Collections.emptyMap(), new NStringEntity(mapping, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new FemmeFulltextException("Index " + indexName + " creation failed", e);
		}
	}


	public boolean indexExists(String indexName) throws FemmeFulltextException {
		Response indexExistenceResponse;
		try {
			indexExistenceResponse = this.client.performRequest("HEAD", "/" + indexName);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeFulltextException("Index " + indexName + " existence check failed", e);
		}
		return indexExistenceResponse.getStatusLine().getStatusCode() != 404;
	}

	boolean aliasExists(String indexAlias) throws FemmeFulltextException {
		Response aliasExistenceResponse;
		try {
			aliasExistenceResponse = this.client.performRequest("HEAD", "/_alias/" + indexAlias);
		} catch (IOException e) {
			throw new FemmeFulltextException("Index alias " + indexAlias + " existence check failed", e);
		}
		return aliasExistenceResponse.getStatusLine().getStatusCode() != 404;
	}

	void createIndexAliasAssociation(String indexName) throws FemmeFulltextException {
		String indexAliasAssociationRequest = "{" +
				"\"actions\":[" +
					"{" +
						"\"add\":{" +
							"\"index\":\"" + indexName + "\", \"alias\":\"" + this.indexAlias + "\"" +
						"}" +
					"}" +
				"]" +
			"}" ;
		HttpEntity entity = new NStringEntity(indexAliasAssociationRequest, ContentType.APPLICATION_JSON);
		try {
			this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new FemmeFulltextException("Index " + indexName + " association with alias " + this.indexAlias + " failed", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	String swapIndex(String newIndexName) throws FemmeFulltextException {
		String oldIndexName = getIndexByAlias(this.indexAlias);
		String indexSwapRequest = "{" +
				"\"actions\":[" +
					"{" +
						"\"add\":{" +
							"\"insert\":\"" + newIndexName + "\"," +
							" \"alias\":\"" + this.indexAlias + "\"" +
						"}" +
					"}," +
					"{" +
						"\"remove_index\":{" +
							"\"insert\":\"" + oldIndexName + "\"" +
						"}" +
					"}" +
				"]" +
			"}" ;
		HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
		try {
			this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new FemmeFulltextException("Metadata insert swaping failed: " + oldIndexName + " -> " + newIndexName, e);
		}

		this.indexAliasCreated.compareAndSet(false, true);

		return oldIndexName;
	}

	void swapWithAliasOldIndices(Set<String> newIndices) throws FemmeFulltextException {
		List<String> oldIndices = getIndicesByAlias(this.indexAlias);
		String indexSwapRequest = "{\"actions\":[" +
				newIndices.stream().map(newIndex -> "\"" + newIndex + "\"")
						.collect(Collectors.joining(",",
						"{\"add\":{\"indices\": [",
						"], \"alias\": \"" + this.indexAlias + "\"}}")) +
				(newIndices.size() > 0 && oldIndices.size() > 0 ? "," : "") +
				oldIndices.stream().map(oldIndex -> "{" +
						"\"remove_index\":{" +
							"\"index\":\"" + oldIndex + "\"" +
						"}" +
					"}").collect(Collectors.joining(",")) +
				"]" +
			"}";

		logger.info("Swap request: " + indexSwapRequest);

		HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
		try {
			this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new FemmeFulltextException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
					"new indices: [" + String.join(",", newIndices) + "]", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	void swapWithAliasOldIndices(Set<String> oldIndices, Set<String> newIndices) throws FemmeFulltextException {
		//List<String> oldIndices = getIndicesByAlias(this.indexAlias);
		String indexSwapRequest = "{\"actions\":[" +
				"{\"add\": {" +
					"\"indices\": [" +
						newIndices.stream().map(newIndex -> "\"" + newIndex + "\"").collect(Collectors.joining(",")) +
					"], \"alias\": \"" + this.indexAlias + "\"}" +
				"}" +
				(oldIndices.size() > 0 ? "," : "") +
				oldIndices.stream().map(oldIndex -> "{" +
					"\"remove_index\":{" +
						"\"insert\":\"" + oldIndex + "\"" +
					"}" +
				"}").collect(Collectors.joining(",")) +
			"]" +
		"}";

		logger.info("Swap request: " + indexSwapRequest);

		HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
		try {
			this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new FemmeFulltextException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
					"new indices: [" + String.join(",", newIndices) + "]", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	String getIndexByAlias(String indexAlias) throws FemmeFulltextException {
		Response indexNameResponse;
		try {
			indexNameResponse = this.client.performRequest("GET", "/_alias/" + indexAlias);
		} catch (IOException e) {
			throw new FemmeFulltextException("Index association retrieval for insert alias " + indexAlias + " failed", e);
		}

		Map<String, Object> indexNameResponseMap;
		try {
			indexNameResponseMap = mapper.readValue(indexNameResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new FemmeFulltextException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
		}

		String aliasIndexName;
		if (indexNameResponseMap == null || indexNameResponseMap.isEmpty()) {
			throw new FemmeFulltextException("No insert associated with insert alias " + indexAlias);
		} else if (indexNameResponseMap.size() > 1) {
			throw new FemmeFulltextException("Multiple indices associated with insert alias " + indexAlias);
		} else {
			aliasIndexName = new ArrayList<>(indexNameResponseMap.keySet()).stream().sorted().findFirst().get();
		}
		return aliasIndexName;
	}

	List<String> getIndicesByAlias(String indexAlias) throws FemmeFulltextException {
		Response indexNameResponse;
		try {
			indexNameResponse = this.client.performRequest("GET", "/*/_alias/" + indexAlias);
		} catch (IOException e) {
			throw new FemmeFulltextException("Indices association retrieval for insert alias " + indexAlias + " failed", e);
		}

		Map<String, Object> indicesResponseMap;
		try {
			indicesResponseMap = mapper.readValue(indexNameResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new FemmeFulltextException("Deserialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
		}

		return new ArrayList<>(indicesResponseMap.keySet());
	}

	String findIndex(String indexName) throws FemmeFulltextException {
		Response response;
		try {
			response = this.client.performRequest("GET", "/" + indexName + "*/_settings");
		} catch (IOException e) {
			throw new FemmeFulltextException("Index search for " + indexName + " failed", e);
		}
		Map<String, Object> indices;
		try {
			indices = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new FemmeFulltextException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
		}
		return indices.keySet().stream().findFirst().orElse(null);
	}

	public boolean mappingExists(String indexName) throws FemmeFulltextException {
		Response mapping;
		try {
			mapping = this.client.performRequest(
				"HEAD",
				"/" + indexName + "/_mapping/" + ElasticFulltextIndexClient.ELASTICSEARCH_TYPE);
		} catch (IOException e) {
			throw new FemmeFulltextException("Mapping existence check failed", e);
		}
		return mapping.getStatusLine().getStatusCode() != 404;
	}

	public void insert(String document) throws FemmeFulltextException {
		insert(document, ElasticFulltextIndexClient.ELASTICSEARCH_INDEX_ALIAS);
	}

	public void insert(String document, String indexName) throws FemmeFulltextException {
		Response indexedDocument;
		try {
			indexedDocument = this.client.performRequest(
					"POST",
					"/" + indexName + "/" + ElasticFulltextIndexClient.ELASTICSEARCH_TYPE,
					Collections.emptyMap(),
					new NStringEntity(document, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new FemmeFulltextException("Indexing failed", e);
		}
	}

	public void delete(String id) throws FemmeFulltextException {
		delete(id, ElasticFulltextIndexClient.ELASTICSEARCH_INDEX_ALIAS);
	}

	public void delete(String id, String indexName) throws FemmeFulltextException {
		try {
			this.client.performRequest("DELETE", "/" + indexName + "/" + ElasticFulltextIndexClient.ELASTICSEARCH_TYPE + "/" + id);
		} catch (IOException e) {
			throw new FemmeFulltextException("Document deletion failed [" + id + "]", e);
		}
	}

	public void deleteByQuery(String query) throws FemmeFulltextException {
		deleteByQuery(query, ElasticFulltextIndexClient.ELASTICSEARCH_INDEX_ALIAS);
	}

	public void deleteByQuery(String query, String indexName) throws FemmeFulltextException {
		try {
			this.client.performRequest(
					"POST",
					"/" + indexName + "/" + ElasticFulltextIndexClient.ELASTICSEARCH_TYPE + "/_delete_by_query",
					Collections.emptyMap(),
					new NStringEntity(query, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new FemmeFulltextException("Delete by elementId failed [" + query + "]", e);
		}
	}

	public List<ElasticResponseHit> search(String query, String indexName) throws FemmeFulltextException {
		try {
			Response response = this.client.performRequest(
					"POST",
					"/" + indexName + "/" + ElasticFulltextIndexClient.ELASTICSEARCH_TYPE + "/_search",
					Collections.emptyMap(),
					new NStringEntity(query, ContentType.APPLICATION_JSON));


			ElasticResponseContent content = mapper.readValue(IOUtils.toString(response.getEntity().getContent(),
					Charset.defaultCharset()), ElasticResponseContent.class);

			return content.getHits().getHits();
		} catch (IOException e) {
			throw new FemmeFulltextException("Search failed [" + query + "]", e);
		}
	}
	
	public <T> List<T> search(SearchSourceBuilder query, Class<T> resultClass) throws SemanticSearchException {
		SearchRequest searchRequest = new SearchRequest();
		query.size(1000);
		searchRequest.source(query);
		
		SearchResponse searchResponse;
		try {
			searchResponse = this.highLevelClient.search(searchRequest);
			return Arrays.stream(searchResponse.getHits().getHits()).map(hit -> {
				try {
					return mapper.readValue(hit.getSourceAsString(), resultClass);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
		} catch (IOException e) {
			throw new SemanticSearchException("Search failed [" + query.toString() + "]", e);
		}
	}
	
	public List<String> aggregate(SearchSourceBuilder query) throws SemanticSearchException {
		query.aggregation(AggregationBuilders.terms("unique_by_name").field("name.keyword"));
		SearchRequest searchRequest = new SearchRequest();
		query.size(1000);
		searchRequest.source(query);
		
		SearchResponse searchResponse;
		try {
			searchResponse = this.highLevelClient.search(searchRequest);
			return getAggregationTerms(searchResponse);
		} catch (IOException e) {
			throw new SemanticSearchException("Search failed [" + query.toString() + "]", e);
		}
	}
	
	private List<String> getAggregationTerms(SearchResponse searchResponse) {
		Terms aggregationResult = searchResponse.getAggregations().get("unique_by_name");
		return aggregationResult.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
	}
	
	public Map<Float, Set<String>> searchUniqueByScore(SearchSourceBuilder query) throws SemanticSearchException {
		SearchRequest searchRequest = new SearchRequest();
		query.size(1000);
		searchRequest.source(query);
		
		SearchResponse searchResponse;
		try {
			searchResponse = this.highLevelClient.search(searchRequest);
			return getHitsAsScoreMapWithUniqueHits(searchResponse);
		} catch (IOException e) {
			throw new SemanticSearchException("Search failed [" + query.toString() + "]", e);
		}
	}
	
	private Map<Float, Set<String>> getHitsAsScoreMapWithUniqueHits(SearchResponse searchResponse) {
		Map<Float, Set<String>> hits = new HashMap<>();
		
		for (SearchHit searchHit: searchResponse.getHits().getHits()) {
			//FulltextDocument doc = serializeSearchHit(searchHit, FulltextDocument.class);
			if (! hits.containsKey(searchHit.getScore())) {
				hits.put(searchHit.getScore(), new HashSet<>(Arrays.asList(searchHit.getSourceAsMap().get("name").toString())));
			} else {
				hits.get(searchHit.getScore()).add(searchHit.getSourceAsMap().get("name").toString());
			}
		}
		return hits;
	}
	
	private Map<String, Float> getHitsAsMap(SearchResponse searchResponse) {
		Map<String, Float> hits = new HashMap<>();
		for (SearchHit searchHit: searchResponse.getHits().getHits()) {
			FulltextDocument doc = serializeSearchHit(searchHit, FulltextDocument.class);
			if (doc != null) {
				if (! hits.containsKey(doc.getFulltextField("name").toString())) {
					hits.put(doc.getFulltextField("name").toString(), searchHit.getScore());
				}
			}
		}
		return hits;
	}
	
	private <T> T serializeSearchHit(SearchHit searchHit, Class<T> resultClass) {
		try {
			return mapper.readValue(searchHit.getSourceAsString(), resultClass);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
