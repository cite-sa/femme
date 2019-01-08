package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.ElasticResponseHit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ElasticMetadataIndexDatastoreRepository {
	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastoreRepository.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String ELASTICSEARCH_HOST_NAME = "localhost";
	private static final int ELASTICSEARCH_PORT = 9200;
	private static final String ELASTICSEARCH_INDEX_ALIAS = "metadataindex";
	private static final String ELASTICSEARCH_TYPE = "xml";

	private RestHighLevelClient client;
	private String indexAlias;
	private AtomicBoolean indexAliasCreated;

	public ElasticMetadataIndexDatastoreRepository() throws MetadataIndexException {
		this(ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_HOST_NAME,
				ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_PORT,
				ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_INDEX_ALIAS);
	}

	@Inject
	public ElasticMetadataIndexDatastoreRepository(String hostName, int port, String indexAlias) throws MetadataIndexException {
		this.indexAlias = indexAlias;
		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostName, port, "http")));
		this.indexAliasCreated = new AtomicBoolean(aliasExists(this.indexAlias));

		try {
			if (! indicesExist(this.indexAlias)) {
				//String indexName = createIndex();
				//createIndexAliasAssociation(indexName);
				createIndex(generateIndexName(), this.indexAlias);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	boolean isIndexAliasCreated() {
		return this.indexAliasCreated.get();
	}

	public void close() throws IOException {
		this.client.close();
	}

	public RestClient get() {
		return client.getLowLevelClient();
	}

	public String getIndexAlias() {
		return this.indexAlias;
	}

	public String getIndexPrefix(String metadataSchemaId) {
		return this.indexAlias + metadataSchemaId;
	}

	public String getFullIndexName(String metadataSchemaId, String uniqueId) {
		return this.indexAlias + metadataSchemaId + "_" + uniqueId;
	}
	
	private String generateIndexName() {
		return this.indexAlias + "_" + Instant.now().toEpochMilli();
	}

	public void createIndex(String indexName) throws MetadataIndexException {
		try {
			CreateIndexRequest request = new CreateIndexRequest(indexName);
			request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
			
			this.client.indices().create(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new MetadataIndexException("Index " + indexName + " creation failed", e);
		}
	}
	
	public void createIndex(String indexName, String indexAlias) throws IOException {
		CreateIndexRequest request = new CreateIndexRequest(indexName);
		
		request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
		request.alias(new Alias(indexAlias));
		
		this.client.indices().create(request, RequestOptions.DEFAULT);
	}
	
	public void createIndexAndAssociateWithAlias(String indexName) throws MetadataIndexException {
		try {
			CreateIndexRequest request = new CreateIndexRequest(indexName);
			request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
			request.alias(new Alias(this.indexAlias));
			
			this.client.indices().create(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new MetadataIndexException("Index " + indexName + " creation failed", e);
		}
	}
	
	public boolean indicesExist(String... indices) throws IOException {
		GetIndexRequest request = new GetIndexRequest();
		request.indices(indices);
		
		return client.indices().exists(request, RequestOptions.DEFAULT);
	}

	boolean aliasExists(String indexAlias) throws MetadataIndexException {
		try {
			GetAliasesRequest request = new GetAliasesRequest(indexAlias);
			return client.indices().existsAlias(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new MetadataIndexException("Index alias " + indexAlias + " existence check failed", e);
		}
	}

	void createIndexAliasAssociation(String indexName) throws MetadataIndexException {
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
			this.client.getLowLevelClient().performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Index " + indexName + " association with alias " + this.indexAlias + " failed", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	String swapIndex(String newIndexName) throws MetadataIndexException {
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
			this.client.getLowLevelClient().performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Metadata insert swaping failed: " + oldIndexName + " -> " + newIndexName, e);
		}

		this.indexAliasCreated.compareAndSet(false, true);

		return oldIndexName;
	}

	void swapWithAliasOldIndices(Set<String> newIndices) throws MetadataIndexException {
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
			this.client.getLowLevelClient().performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
					"new indices: [" + String.join(",", newIndices) + "]", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	void swapWithAliasOldIndices(Set<String> oldIndices, Set<String> newIndices) throws MetadataIndexException {
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
			this.client.getLowLevelClient().performRequest("POST", "/_aliases", Collections.emptyMap(), entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Indices swap failed: old indices: [" + String.join(",", oldIndices) + "], " +
					"new indices: [" + String.join(",", newIndices) + "]", e);
		}

		this.indexAliasCreated.compareAndSet(false, true);
	}

	String getIndexByAlias(String indexAlias) throws MetadataIndexException {
		Response indexNameResponse;
		try {
			indexNameResponse = this.client.getLowLevelClient().performRequest("GET", "/_alias/" + indexAlias);
		} catch (IOException e) {
			throw new MetadataIndexException("Index association retrieval for insert alias " + indexAlias + " failed", e);
		}

		Map<String, Object> indexNameResponseMap;
		try {
			indexNameResponseMap = mapper.readValue(indexNameResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new MetadataIndexException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
		}

		String aliasIndexName;
		if (indexNameResponseMap == null || indexNameResponseMap.isEmpty()) {
			throw new MetadataIndexException("No insert associated with insert alias " + indexAlias);
		} else if (indexNameResponseMap.size() > 1) {
			throw new MetadataIndexException("Multiple indices associated with insert alias " + indexAlias);
		} else {
			aliasIndexName = new ArrayList<>(indexNameResponseMap.keySet()).stream().sorted().findFirst().get();
		}
		return aliasIndexName;
	}

	List<String> getIndicesByAlias(String indexAlias) throws MetadataIndexException {
		try {
			GetAliasesRequest request = new GetAliasesRequest(indexAlias);
			GetAliasesResponse response = client.indices().getAlias(request, RequestOptions.DEFAULT);
			return new ArrayList<>(response.getAliases().keySet());
		} catch (IOException e) {
			throw new MetadataIndexException("Indices association retrieval for index alias " + indexAlias + " failed", e);
		}
	}

	void createMapping(MetadataSchema metadataSchema, String indexName) throws MetadataIndexException {
		String dynamicTemplate = metadataSchema.getSchema().stream().filter(JSONPath::isArray)
				.map(nestedPath -> "{" +
					"\"arrays_as_nested\":{" +
						"\"path_match\":\"value." + nestedPath.getPath() + "\"," +
						"\"mapping\":{" +
							"\"type\":\"nested\"" +
						"}" +
					"}" +
				"}").collect(Collectors.joining(",", "{\"dynamic_templates\":[", "]}"));
		/*MappingTree mappingTree = new MappingTree();
		String nestedValueMapping = mappingTree.buildMapping(schema.getSchema().stream().filter(JSONPath::isArray).map(JSONPath::getPath).collect(Collectors.toList()));*/

		HttpEntity entity = new NStringEntity(dynamicTemplate, ContentType.APPLICATION_JSON);
		try {
			this.client.getLowLevelClient().performRequest(
					"PUT",
					//"/" + indexName + "/_mapping/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE_PREFIX + schema.getId(),
					"/" + indexName + "/_mapping/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE,
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			throw new MetadataIndexException("ElasticSearch dynamic template creation failed");
		}
	}

	boolean mappingExists(String indexName) throws MetadataIndexException {
		try {
			GetMappingsRequest request = new GetMappingsRequest();
			request.indices(indexName);
			request.types(ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE);
			GetMappingsResponse getMappingResponse = this.client.indices().getMapping(request, RequestOptions.DEFAULT);
			
			ImmutableOpenMap<String, MappingMetaData> index = getMappingResponse.mappings().get(indexName);
			if (index == null) {
				return false;
			}
			
			return index.get(ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE) != null;
		} catch (IOException e) {
			throw new MetadataIndexException("Mapping existence check failed", e);
		}
		
		/*Response mapping;
		try {
			mapping = this.client.getLowLevelClient().performRequest(
				"HEAD",
				"/" + indexName + "/_mapping/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE);
		} catch (IOException e) {
			throw new MetadataIndexException("Mapping existence check failed", e);
		}
		return mapping.getStatusLine().getStatusCode() != javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();*/
	}

	public void insert(String document, String indexName) throws MetadataIndexException {
		try {
			IndexRequest request = new IndexRequest(indexName, ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE).source(document, XContentType.JSON);
			this.client.index(request, RequestOptions.DEFAULT);
			/*this.client.getLowLevelClient().performRequest(
					"POST",
					"/" + indexName + "/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE,
					Collections.emptyMap(),
					new NStringEntity(document, ContentType.APPLICATION_JSON));*/
		} catch (Exception e) {
			throw new MetadataIndexException("Indexing failed:\n" + document, e);
		}
	}

	List<SearchHit> search(Map<String, String> fieldsAndValues) throws MetadataIndexException {
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		fieldsAndValues.entrySet().stream().map(fieldAndValue -> QueryBuilders.termQuery(fieldAndValue.getKey(), fieldAndValue.getValue()))
			.forEach(boolQueryBuilder::must);
		searchSourceBuilder.query(boolQueryBuilder);
		
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse;
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new MetadataIndexException("Search query failed", e);
		}
		
		return Arrays.asList(searchResponse.getHits().getHits());
		
		/*String searchQuery = "{" +
				"\"query\":{" +
					"\"bool\":{" +
						"\"must\":" +
							fieldsAndValues.entrySet().stream()
								.map(fieldAndValue -> "{\"term\":{\"" + fieldAndValue.getKey() + "\":\"" + fieldAndValue.getValue() + "\"}}")
								.collect(Collectors.joining(",", "[", "]")) +
					"}" +
				"}" +
			"}";

		Response response;
		try {
			response = this.client.getLowLevelClient().performRequest(
					"POST",
					"/" + this.indexAlias + "/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE + "/_search",
					Collections.emptyMap(),
					new NStringEntity(searchQuery, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new MetadataIndexException("Search query failed", e);
		}

		ElasticResponseContent responseContent;
		try {
			responseContent = mapper.readValue(response.getEntity().getContent(), ElasticResponseContent.class);
		} catch (IOException e) {
			throw new MetadataIndexException("Serialization of insert association retrieval response for insert alias " + indexAlias + " failed", e);
		}
		return responseContent.getHits().getHits();*/
	}

	void delete(String id, String indexName) throws MetadataIndexException {
		try {
			DeleteRequest request = new DeleteRequest(indexName, ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE, id);
			DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new MetadataIndexException("Delete " + id + " failed", e);
		}
	}

	void deleteByQuery(Map<String, String> fieldsAndValues, String indexName) throws MetadataIndexException {
		String deleteQuery = "{" +
				"\"query\":{" +
					"\"bool\":{" +
						"\"must\":" +
							fieldsAndValues.entrySet().stream()
									.map(fieldAndValue -> "{\"term\":{\"" + fieldAndValue.getKey() + "\":\"" + fieldAndValue.getValue() + "\"}}")
									.collect(Collectors.joining(",", "[", "]")) +
					"}" +
				"}" +
			"}";

		try {
			this.client.getLowLevelClient().performRequest(
					"POST",
					"/" + indexName + "/" + ElasticMetadataIndexDatastoreRepository.ELASTICSEARCH_TYPE,
					Collections.emptyMap(),
					new NStringEntity(deleteQuery, ContentType.APPLICATION_JSON));
		} catch (IOException e) {
			throw new MetadataIndexException("Delete by query failed", e);
		}
	}
}
