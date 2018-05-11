package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

//@Component
public class ElasticsearchClient {
	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClient.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String ELASTICSEARCH_HOST_NAME = "localhost";
	private static final int ELASTICSEARCH_PORT = 9200;
	private static final String ELASTICSEARCH_INDEX_ALIAS = "semantic_search";
	private static final String ELASTICSEARCH_TYPE = "taxonomies";
	
	private RestHighLevelClient client;
	private String indexName;
	private String indexType;
	private AtomicBoolean indexAliasCreated;
	
	public ElasticsearchClient() throws UnknownHostException {
		this(ElasticsearchClient.ELASTICSEARCH_HOST_NAME,
			ElasticsearchClient.ELASTICSEARCH_PORT,
			ElasticsearchClient.ELASTICSEARCH_INDEX_ALIAS,
			ElasticsearchClient.ELASTICSEARCH_TYPE);
	}
	
	public ElasticsearchClient(String hostName, int port) throws UnknownHostException {
		this(hostName, port, ElasticsearchClient.ELASTICSEARCH_INDEX_ALIAS, ElasticsearchClient.ELASTICSEARCH_TYPE);
	}
	
	public ElasticsearchClient(String hostName, int port, String indexName) throws UnknownHostException {
		this(hostName, port, indexName, ElasticsearchClient.ELASTICSEARCH_TYPE);
	}
	
	public ElasticsearchClient(String hostName, int port, String indexName, String indexType) throws UnknownHostException {
		this.indexName = indexName;
		this.indexType = indexType;
		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostName, port, "http")));
		//this.client = RestClient.builder(new HttpHost(hostName, port, "http")).build();
	}
	
	public void close() throws IOException {
		System.out.println("closing");
		this.client.close();
	}
	
	boolean isIndexAliasCreated() {
		return this.indexAliasCreated.get();
	}
	
	public RestHighLevelClient get() {
		return client;
	}
	
	/*public String getFullIndexName(String metadataSchemaId, String uniqueId) {
		return this.indexAlias + metadataSchemaId + "_" + uniqueId;
	}*/
	
	/*public String createIndex() throws IOException {
		String indexName = this.indexAlias + "_" + Instant.now().toEpochMilli();
		this.client.performRequest("PUT", "/" + indexName);
		return indexName;
	}*/
	
	/*public void createIndex() throws SemanticSearchException {
		createIndex(this.indexName);
	}
	
	public void createIndex(String indexName) throws SemanticSearchException {
		createIndex(new CreateIndexRequest(this.indexName));
	}
	
	public void createIndex(String indexName, String mapping) throws SemanticSearchException {
		CreateIndexRequest request = new CreateIndexRequest(this.indexName);
		if (!Strings.isNullOrEmpty(mapping)) {
			request.mapping(this.indexType, mapping, XContentType.JSON);
		}
		
		createIndex(request);
	}
	
	private void createIndex(CreateIndexRequest request) throws SemanticSearchException {
		CreateIndexResponse response;
		try {
			response = this.client.indices().create(request);
		} catch (IOException e) {
			throw new SemanticSearchException("Index creation failed", e);
		}
	}*/
	
	
	//public boolean indexExists(String indexName) throws SemanticSearchException {
	//	Response indexExistenceResponse;
	//	try {
	//		indexExistenceResponse = this.client.performRequest("HEAD", "/" + indexName);
	//	} catch (IOException e) {
	//		logger.error(e.getMessage(), e);
	//		throw new SemanticSearchException("Index " + indexName + " existence check failed", e);
	//	}
	//	return indexExistenceResponse.getStatusLine().getStatusCode() != javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
	//}
	//
	//boolean aliasExists(String indexAlias) throws SemanticSearchException {
	//	Response aliasExistenceResponse;
	//	try {
	//		aliasExistenceResponse = this.client.performRequest("HEAD", "/_alias/" + indexAlias);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Index alias " + indexAlias + " existence check failed", e);
	//	}
	//	return aliasExistenceResponse.getStatusLine().getStatusCode() != javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
	//}
	//
	//void createIndexAliasAssociation(String indexName) throws SemanticSearchException {
	//	String indexAliasAssociationRequest = "{" +
	//											  "\"actions\":[" +
	//											  "{" +
	//											  "\"add\":{" +
	//											  "\"index\":\"" + indexName + "\", \"alias\":\"" + this.indexAlias + "\"" +
	//											  "}" +
	//											  "}" +
	//											  "]" +
	//											  "}";
	//	HttpEntity entity = new NStringEntity(indexAliasAssociationRequest, ContentType.APPLICATION_JSON);
	//	try {
	//		this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Index " + indexName + " association with alias " + this.indexAlias + " failed", e);
	//	}
	//
	//	this.indexAliasCreated.compareAndSet(false, true);
	//}
	//
	//String swapIndex(String newIndexName) throws SemanticSearchException {
	//	String oldIndexName = getIndexByAlias(this.indexAlias);
	//	String indexSwapRequest = "{" +
	//								  "\"actions\":[" +
	//								  "{" +
	//								  "\"add\":{" +
	//								  "\"insert\":\"" + newIndexName + "\"," +
	//								  " \"alias\":\"" + this.indexAlias + "\"" +
	//								  "}" +
	//								  "}," +
	//								  "{" +
	//								  "\"remove_index\":{" +
	//								  "\"insert\":\"" + oldIndexName + "\"" +
	//								  "}" +
	//								  "}" +
	//								  "]" +
	//								  "}";
	//	HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
	//	try {
	//		this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Metadata insert swaping failed: " + oldIndexName + " -> " + newIndexName, e);
	//	}
	//
	//	this.indexAliasCreated.compareAndSet(false, true);
	//
	//	return oldIndexName;
	//}
	//
	//void swapWithAliasOldIndices(Set<String> newIndices) throws SemanticSearchException {
	//	List<String> oldIndices = getIndicesByAlias(this.indexAlias);
	//	String indexSwapRequest = "{\"actions\":[" +
	//								  newIndices.stream().map(newIndex -> "\"" + newIndex + "\"")
	//									  .collect(Collectors.joining(",",
	//										  "{\"add\":{\"indices\": [",
	//										  "], \"alias\": \"" + this.indexAlias + "\"}}")) +
	//								  (newIndices.size() > 0 && oldIndices.size() > 0 ? "," : "") +
	//								  oldIndices.stream().map(oldIndex -> "{" +
	//																		  "\"remove_index\":{" +
	//																		  "\"index\":\"" + oldIndex + "\"" +
	//																		  "}" +
	//																		  "}").collect(Collectors.joining(",")) +
	//								  "]" +
	//								  "}";
	//
	//	logger.info("Swap request: " + indexSwapRequest);
	//
	//	HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
	//	try {
	//		this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
	//											  "new indices: [" + String.join(",", newIndices) + "]", e);
	//	}
	//
	//	this.indexAliasCreated.compareAndSet(false, true);
	//}
	//
	//void swapWithAliasOldIndices(Set<String> oldIndices, Set<String> newIndices) throws SemanticSearchException {
	//	//List<String> oldIndices = getIndicesByAlias(this.indexAlias);
	//	String indexSwapRequest = "{\"actions\":[" +
	//		"{\"add\": {" +
	//			"\"indices\": [" +
	//				newIndices.stream().map(newIndex -> "\"" + newIndex + "\"").collect(Collectors.joining(",")) +
	//					"], \"alias\": \"" + this.indexAlias + "\"}" +
	//				"}" +
	//				(oldIndices.size() > 0 ? "," : "") +
	//				oldIndices.stream().map(oldIndex -> "{" +
	//					"\"remove_index\":{" +
	//						"\"insert\":\"" + oldIndex + "\"" +
	//					"}" +
	//				"}").collect(Collectors.joining(",")) +
	//			"]" +
	//		"}";
	//
	//	logger.info("Swap request: " + indexSwapRequest);
	//
	//	HttpEntity entity = new NStringEntity(indexSwapRequest, ContentType.APPLICATION_JSON);
	//	try {
	//		this.client.performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
	//											  "new indices: [" + String.join(",", newIndices) + "]", e);
	//	}
	//
	//	this.indexAliasCreated.compareAndSet(false, true);
	//}
	//
	//String getIndexByAlias(String indexAlias) throws SemanticSearchException {
	//	Response indexNameResponse;
	//	try {
	//		indexNameResponse = this.client.performRequest("GET", "/_alias/" + indexAlias);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Index association retrieval for insert alias " + indexAlias + " failed", e);
	//	}
	//
	//	Map<String, Object> indexNameResponseMap;
	//	try {
	//		indexNameResponseMap = mapper.readValue(indexNameResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>() {
	//		});
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
	//	}
	//
	//	String aliasIndexName;
	//	if (indexNameResponseMap == null || indexNameResponseMap.isEmpty()) {
	//		throw new SemanticSearchException("No insert associated with insert alias " + indexAlias);
	//	} else if (indexNameResponseMap.size() > 1) {
	//		throw new SemanticSearchException("Multiple indices associated with insert alias " + indexAlias);
	//	} else {
	//		aliasIndexName = new ArrayList<>(indexNameResponseMap.keySet()).stream().sorted().findFirst().get();
	//	}
	//	return aliasIndexName;
	//}
	//
	//List<String> getIndicesByAlias(String indexAlias) throws SemanticSearchException {
	//	Response indexNameResponse;
	//	try {
	//		indexNameResponse = this.client.performRequest("GET", "/*/_alias/" + indexAlias);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Indices association retrieval for insert alias " + indexAlias + " failed", e);
	//	}
	//
	//	Map<String, Object> indicesResponseMap;
	//	try {
	//		indicesResponseMap = mapper.readValue(indexNameResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>() {
	//		});
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Deserialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
	//	}
	//
	//	return new ArrayList<>(indicesResponseMap.keySet());
	//}
	//
	//String findIndex(String indexName) throws SemanticSearchException {
	//	Response response;
	//	try {
	//		response = this.client.performRequest("GET", "/" + indexName + "*/_settings");
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Index search for " + indexName + " failed", e);
	//	}
	//	Map<String, Object> indices;
	//	try {
	//		indices = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>() {
	//		});
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
	//	}
	//	return indices.keySet().stream().findFirst().orElse(null);
	//}
	//
	//public boolean mappingExists(String indexName) throws SemanticSearchException {
	//	Response mapping;
	//	try {
	//		mapping = this.client.performRequest(
	//			"HEAD",
	//			"/" + indexName + "/_mapping/" + this.indexType);
	//	} catch (IOException e) {
	//		throw new SemanticSearchException("Mapping existence check failed", e);
	//	}
	//	return mapping.getStatusLine().getStatusCode() != javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
	//}
	
	/*public void insert(String document) throws SemanticSearchException {
		insert(document, this.indexAlias);
	}*/
	
	/*public void insert(String document, String indexName) throws SemanticSearchException {
		try {
			this.client.performRequest(
				"POST",
				"/" + indexName + "/" + this.indexType,
				Collections.emptyMap(),
				new NStringEntity(document, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new SemanticSearchException("Indexing failed", e);
		}
	}*/
	
	public void insert(String document) throws SemanticSearchException {
		IndexRequest request = new IndexRequest(this.indexName, this.indexType);
		try {
			request.source(document, XContentType.JSON);
			this.client.index(request);
		} catch (IOException e) {
			throw new SemanticSearchException("Error inserting document", e);
		}
	}
	
	public void insert(List<Map<String, Object>> documents) throws SemanticSearchException {
		BulkRequest request = new BulkRequest();
		for (Map<String, Object> document: documents) {
			request.add(new IndexRequest(this.indexName, this.indexType).source(document));
		}
		
		try {
			this.client.bulk(request);
		} catch (IOException e) {
			throw new SemanticSearchException("Error bulk inserting document", e);
		}
	}
	
	/*public void delete(String id) throws SemanticSearchException {
		delete(id, this.indexAlias);
	}
	
	public void delete(String id, String indexName) throws SemanticSearchException {
		try {
			this.client.performRequest("DELETE", "/" + indexName + "/" + this.indexType + "/" + id);
		} catch (IOException e) {
			throw new SemanticSearchException("Document deletion failed [" + id + "]", e);
		}
	}
	
	public void deleteByQuery(String query) throws SemanticSearchException {
		deleteByQuery(query, this.indexAlias);
	}
	
	public void deleteByQuery(String query, String indexName) throws SemanticSearchException {
		try {
			this.client.performRequest(
				"POST",
				"/" + indexName + "/" + this.indexType + "/_delete_by_query",
				Collections.emptyMap(),
				new NStringEntity(query, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new SemanticSearchException("Delete by elementId failed [" + query + "]", e);
		}
	}*/
	
	/*public <T> List<ElasticResponseHit<T>> search(String query, String indexName) throws SemanticSearchException {
		try {
			Response response = this.client.performRequest(
				"POST",
				"/" + indexName + "/" + this.indexType + "/_search",
				Collections.emptyMap(),
				new NStringEntity(query, ContentType.APPLICATION_JSON));
			
			
			ElasticResponseContent<T> content = mapper.readValue(IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()), new TypeReference<ElasticResponseContent<T>>() {
			});
			
			return content.getHits().getHits();
			//response.
		} catch (IOException e) {
			throw new SemanticSearchException("Search failed [" + query + "]", e);
		}
		
		
	}
	
	public <T> List<ElasticResponseHit<T>> getByQuery(String query, Class<T> clazz) throws SemanticSearchException {
		try {
			String endpoint = "/" + this.indexAlias + "/" + this.indexType + "/_search";
			HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
			Response response = this.client.performRequest("GET", endpoint, Collections.emptyMap(), entity);
			ElasticResponseContent<T> content = mapper.readValue(IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()),
				mapper.getTypeFactory().constructParametricType(ElasticResponseContent.class, clazz));
			
			return content.getHits().getHits();
		} catch (IOException e) {
			throw new SemanticSearchException("Search failed [" + query + "]", e);
		}
	}*/
	public <T> List<T> getByQuery(SearchSourceBuilder query, Class<T> resultClass) throws SemanticSearchException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(query);
		
		SearchResponse searchResponse;
		try {
			searchResponse = this.client.search(searchRequest);
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
	
}

