package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.utils.Pair;
import gr.cite.femme.engine.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.FilterNodesExpression;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Node;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

public class ElasticMetadataIndexDatastore implements MetadataIndexDatastore {

	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastore.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private final StampedLock lock = new StampedLock();

	private ElasticMetadataIndexDatastoreRepository metadataIndexDatastoreRepository;
	private MetadataSchemaIndexDatastore schemaIndexDatastore;
	private Indices indices;

	//private AtomicBoolean reIndex = new AtomicBoolean(false);
	//private String indexName;


	public ElasticMetadataIndexDatastore(MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException, MetadataIndexException {
		this.metadataIndexDatastoreRepository = new ElasticMetadataIndexDatastoreRepository();
		this.schemaIndexDatastore = schemaIndexDatastore;
		this.indices = new Indices(this.metadataIndexDatastoreRepository.getIndicesByAlias(this.metadataIndexDatastoreRepository.getIndexAlias()));
	}

	public ElasticMetadataIndexDatastore(ElasticMetadataIndexDatastoreRepository metadataIndexDatastoreRepository, MetadataSchemaIndexDatastore schemaIndexDatastore) throws MetadataIndexException {
		this.metadataIndexDatastoreRepository = metadataIndexDatastoreRepository;
		this.schemaIndexDatastore = schemaIndexDatastore;
		this.indices = new Indices(this.metadataIndexDatastoreRepository.getIndicesByAlias(this.metadataIndexDatastoreRepository.getIndexAlias()));
	}

	synchronized Indices getIndices() {
		return indices;
	}

	synchronized void setIndices(Indices indices) {
		this.indices = indices;
	}

	@Override
	@PreDestroy
	public void close() throws IOException {
		metadataIndexDatastoreRepository.close();
	}

	public ReIndexingProcess retrieveReIndexer(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore) {
		return new ElasticReindexingProcess(metadataSchemaIndexDatastore, this, this.metadataIndexDatastoreRepository);
	}

	public void insert(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		String uniqueMetadataIndexName;
		String metadataSchemaId = metadataSchema.getId();
		String randomId = UUID.randomUUID().toString();

		synchronized (this) {
			if (this.indices.compareAndAdd(metadataSchemaId, randomId)) {
				uniqueMetadataIndexName = this.metadataIndexDatastoreRepository.getFullIndexName(metadataSchemaId, randomId);
				this.metadataIndexDatastoreRepository.createIndex(uniqueMetadataIndexName);
				this.metadataIndexDatastoreRepository.createMapping(metadataSchema, uniqueMetadataIndexName);
				this.metadataIndexDatastoreRepository.createIndexAliasAssociation(uniqueMetadataIndexName);
			} else {
				//uniqueMetadataIndexName = this.metadataIndexDatastoreRepository.findIndex(metadataIndexName);
				uniqueMetadataIndexName = this.metadataIndexDatastoreRepository.getFullIndexName(metadataSchemaId, this.indices.getUniqueId(metadataSchemaId));
			}
		}

		/*if (!this.metadataIndexDatastoreRepository.indexExists(timestampedMetadataIndexName)) {
			this.metadataIndexDatastoreRepository.createIndex(timestampedMetadataIndexName);
			this.metadataIndexDatastoreRepository.createIndexAliasAssociation(timestampedMetadataIndexName);
		}*/

		/*if (!this.metadataIndexDatastoreRepository.mappingExists(timestampedMetadataIndexName)) {
			this.metadataIndexDatastoreRepository.createMapping(metadataSchema, timestampedMetadataIndexName);
		}*/

		/*if (!this.metadataIndexDatastoreRepository.indexExists(metadataIndexName)) {
			this.metadataIndexDatastoreRepository.createIndex(metadataIndexName);
			this.metadataIndexDatastoreRepository.createIndexAliasAssociation(metadataIndexName);
		}*/

		String indexableMetadatumSerialized;
		try {
			indexableMetadatumSerialized = mapper.writeValueAsString(indexableMetadatum);
		} catch (JsonProcessingException e) {
			throw new MetadataIndexException("Metadatum serialization failed", e);
		}
		this.metadataIndexDatastoreRepository.insert(indexableMetadatumSerialized, uniqueMetadataIndexName);
	}

	@Override
	public void delete(String metadatumId) throws MetadataIndexException {
		delete("metadatumId", metadatumId);
	}

	@Override
	public void deleteByElementId(String elementId) throws MetadataIndexException {
		delete("elementId", elementId);
	}

	@Override
	public void delete(String field, String value) throws MetadataIndexException {
		Map<String, String> fieldsAndValues = new HashMap<>();
		fieldsAndValues.put(field, value);

		this.metadataIndexDatastoreRepository.search(fieldsAndValues).forEach(hit -> {
			try {
				this.metadataIndexDatastoreRepository.delete(hit.getId(), hit.getIndex());
			} catch (MetadataIndexException e) {
				logger.error(e.getMessage(), e);
			}
		});
	}

	@Override
	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree) throws MetadataIndexException {
		return query(queryTree, true);
	}

	@Override
	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException {
		return query(new ArrayList<>(), queryTree, lazy);
	}

	@Override
	public List<IndexableMetadatum> query(List<String> elementIds, Tree<QueryNode> queryTree) throws MetadataIndexException {
		return query(elementIds, queryTree, true);
	}

	@Override
	public List<IndexableMetadatum> query(List<String> elementIds, Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		
		if (!this.metadataIndexDatastoreRepository.isIndexAliasCreated()) {
			return new ArrayList<>();
		}

		ElasticScrollQuery scrollQuery = new ElasticScrollQuery(this.metadataIndexDatastoreRepository);
		Set<IndexableMetadatum> results = new HashSet<>();
		try {
			//logger.info("ElasticSearch query: " + buildElasticSearchQuery(queryTree, lazy));
			//String searchQuery = buildElasticSearchQuery(queryTree, lazy);
			
			long start, end;
			start = System.currentTimeMillis();
			List<ElasticSearchQuery> searchQueries = buildElasticSearchQuery(elementIds, queryTree);
			end = System.currentTimeMillis();
			logger.info("Elasticsearch query build time: " + (end - start) + " ms");

			
			List<Future<List<IndexableMetadatum>>> futures = new ArrayList<>();
			for (ElasticSearchQuery searchQuery: searchQueries) {
				
				//futures.add(executor.submit(() -> {
				List<IndexableMetadatum> scrollResults = new ArrayList<>();
				
				for (Map.Entry<String, List<String>> searchQueryEntry: searchQuery.getIndicesPerQuery().entrySet()) {
					
					futures.add(executor.submit(() -> {
						long startScroll = System.currentTimeMillis();
						
						try {
							scrollQuery.query(searchQueryEntry.getKey(), searchQuery.getIncludes(), searchQueryEntry.getValue(), lazy);
						} catch (Throwable e) {
							logger.error("Elasticsearch scroll query failed", e);
							return new ArrayList<>();
						}
						
						while (scrollQuery.hasNext()) {
							scrollResults.addAll(scrollQuery.next());
						}
						
						long endScroll = System.currentTimeMillis();
						logger.info("Elasticsearch scroll query total time: " + (endScroll - startScroll) + " ms");
						
						return scrollResults;
					}));
				}
			}
				
			for (Future<List<IndexableMetadatum>> future: futures) {
				results.addAll(future.get());
			}
			
		} catch (Throwable e) {
			throw new MetadataIndexException("Elasticsearch scroll query failed", e);
		}

		logger.debug("Total metadata found: " + results.size());

		return new ArrayList<>(results);
	}

	private List<ElasticSearchQuery> buildElasticSearchQuery(List<String> elementIds, Tree<QueryNode> queryTree) {
		List<ElasticSearchQuery> shoulds = buildShoulds(queryTree.getRoot());

		String elementIdsFilter = !elementIds.isEmpty() ?
				elementIds.stream().map(elementId -> "\"" + elementId +"\"")
						.collect(Collectors.joining(",", "{\"terms\":{\"elementId\":[", "]}},"))
				: "";
		
		ElasticSearchQuery finalQuery = new ElasticSearchQuery();
		
		shoulds.forEach(query -> {
			//ElasticSearchQuery finalQuery = new ElasticSearchQuery();
			
			finalQuery.getIncludes().addAll(query.getIncludes());
			
			query.getIndicesPerQuery().forEach((subQuery, indices) -> finalQuery.addQuery(stringifySubquery(subQuery, elementIdsFilter), indices));
			
		});
		
		return Collections.singletonList(finalQuery);
	}
	
	private String stringifySubquery(String subQuery, String elementIdsFilter) {
		/*return subQuery != null && ! subQuery.isEmpty() ?
				   "\"query\":{" +
					   		"\"bool\":{" +
					   			"\"filter\":[" +
					   				elementIdsFilter +
					   				"{" +
					   					"\"bool\":{" +
					   						"\"should\":[" +
					   							subQuery +
					   						"]" +
					   					"}" +
					   				"}" +
					   			"]" +
					   		"}" +
					   "}" :
			"";*/
		
		return "\"query\":{" +
					"\"bool\":{" +
						"\"filter\":[" +
				   (elementIdsFilter != null ? elementIdsFilter : "" ) +
							"{" +
								"\"bool\":{" +
									"\"should\":[" + (subQuery != null ? subQuery : "") + "]" +
							  	"}" +
							"}" +
				   		"]" +
					"}" +
				"}";
	}

	private List<ElasticSearchQuery> buildShoulds(Node<QueryNode> root) {
		List<ElasticSearchQuery> shoulds = new ArrayList<>();
		Set<String> includes = new HashSet<>();
		buildShould(root, shoulds, includes);
		//shoulds.forEach(should -> should.setIncludes(includes));
		return shoulds;
	}

	private ElasticSearchQuery buildShould(Node<QueryNode> node, List<ElasticSearchQuery> shoulds, Set<String> includes) {
		for (Node<QueryNode> child : node.getChildren()) {
			ElasticSearchQuery childQuery = buildShould(child, shoulds, includes);
			
			if (childQuery != null) {
				shoulds.add(childQuery);
			}
			/*if (child.getData().isFilterPayload()) {
				includes.add(child.getData().getNodePath().toString());
			}*/
		}

		return buildElasticsearchQuery(node);
	}
	
	private ElasticSearchQuery buildElasticsearchQuery(Node<QueryNode> node) {
		QueryNode nodeData = node.getData();
		if (! nodeData.getFilterPath().toString().trim().isEmpty() || nodeData.getValue() != null || nodeData.getFilterNodesExpression().getFilterNodes().size() > 0) {
			return buildTermOrNested(nodeData);
			//} else if (node.getData().getFilterNodes().size() > 0) {
		} else if (node.isLeaf()) {
			return buildIncludes(node);
		}
		
		return null;
	}
	
	private ElasticSearchQuery buildIncludes(Node<QueryNode> node) {
		//includes.add(node.getData().getNodePath().toString());
		ElasticSearchQuery query = new ElasticSearchQuery();
		
		query.getIncludes().add(node.getData().getNodePath().toString());
		
		Map<String, List<String>> indices = new HashMap<>();
		indices.put("", node.getData().getMetadataSchemaIds());
		query.setIndicesPerQuery(indices);
		
		return query;
	}

	private ElasticSearchQuery buildTermOrNested(QueryNode node) {
		ElasticSearchQuery query = new ElasticSearchQuery();
		List<String> nodeMetadataSchemaIds = new ArrayList<>(node.getMetadataSchemaIds());
		
		Map<String, List<String>> metadataSchemaIdsPerNestedPath = getMetadataSchemaIdsPerNestedPath(node, nodeMetadataSchemaIds);
		
		metadataSchemaIdsPerNestedPath.forEach((path, indices) -> query.addQuery(buildNestedQuery(path, node), indices));
		
		query.addQuery(buildFilterQuery(node), nodeMetadataSchemaIds);
		
		if (node.isFilterPayload()) {
			query.getIncludes().add(node.getNodePath().toString());
		}
		if (node.isProjectionNode()) {
			query.getIncludes().add(node.getNodePath().toString() + node.getProjectionPath().toString());
		}

		return query;
	}
	
	private Map<String, List<String>> getMetadataSchemaIdsPerNestedPath(QueryNode node, List<String> nodeMetadataSchemaIds) {
		Map<String, List<String>> metadataSchemaIdsPerNestedPath = new HashMap<>();
		
		/*Map<String, List<String>> map = new HashMap<>();
		this.schemaIndexDatastore.findArrayMetadataIndexPaths().stream()
				.filter(metadataSchema -> node.getMetadataSchemaIds().contains(metadataSchema.getId()))
				.map(metadataSchema -> {
					metadataSchema.getSchema().removeIf(path -> ! node.getNodePath().toString().startsWith(path.getPath()));

					return new Pair<>(
							metadataSchema.getId(),
							metadataSchema.getSchema().stream().map(JSONPath::getPath).sorted(Comparator.comparing(String::length)).findFirst());
				})
				.filter(pair -> pair.getRight().isPresent()).map(pair -> new Pair<>(pair.getLeft(), pair.getRight().get()))
				.forEach(nestedPath -> {
					nodeMetadataSchemaIds.removeIf(nodeMetadataSchemaId -> nodeMetadataSchemaId.equals(nestedPath.getLeft()));

					if (map.containsKey(nestedPath.getRight())) {
						map.get(nestedPath.getRight()).add(nestedPath.getLeft());
					} else {
						List<String> ids = new ArrayList<>();
						ids.add(nestedPath.getLeft());
						map.put(nestedPath.getRight(), ids);
					}
				});*/
		
		this.schemaIndexDatastore.findArrayMetadataIndexPaths(node.getMetadataSchemaIds(), node.getNodePath().toString()).stream()
			.map(metadataSchema -> {
				metadataSchema.getSchema().removeIf(path -> ! node.getNodePath().toString().startsWith(path.getPath()));
				
				Optional<String> shortestPath = metadataSchema.getSchema().stream().map(JSONPath::getPath).min(Comparator.comparing(String::length));
				
				return new Pair<>(metadataSchema.getId(), shortestPath);
			})
			.filter(metadataSchemaIdAndPath -> metadataSchemaIdAndPath.getRight().isPresent())
			.map(metadataSchemaIdAndPath -> new Pair<>(metadataSchemaIdAndPath.getLeft(), metadataSchemaIdAndPath.getRight().get()))
			.forEach(nestedPath -> {
				nodeMetadataSchemaIds.removeIf(nodeMetadataSchemaId -> nodeMetadataSchemaId.equals(nestedPath.getLeft()));
				
				if (metadataSchemaIdsPerNestedPath.containsKey(nestedPath.getRight())) {
					metadataSchemaIdsPerNestedPath.get(nestedPath.getRight()).add(nestedPath.getLeft());
				} else {
					List<String> ids = new ArrayList<>();
					ids.add(nestedPath.getLeft());
					metadataSchemaIdsPerNestedPath.put(nestedPath.getRight(), ids);
				}
			});
		
		return metadataSchemaIdsPerNestedPath;
	}
	
	private String buildNestedQuery(String path, QueryNode node) {
		String filterPath = ("".equals(node.getFilterPath().toString()) ? "" : ".") + node.getFilterPath().toString();
		String filterNodePath = node.getFilterNodesExpression().getFilterNodes().size() > 0 ? "." + node.getFilterNodesExpression().getFilterNodes().get(0).getFilterPath() : "";
		String filterNodeValue = node.getFilterNodesExpression().getFilterNodes().size() > 0 ? node.getFilterNodesExpression().getFilterNodes().get(0).getValue() : "" ;
		
		
		String termPath = "\"value." + node.getNodePath().toString() + filterPath + filterNodePath + ".keyword\"" + ":\"" + filterNodeValue + "\"";
		return "{" +
			"\"nested\" : {" +
				"\"path\": \"value." + path + "\"," +
				"\"query\": {" +
					"\"bool\": {" +
						"\"must\": [" +
							"{" +
								"\"term\": {" +
									/*"\"value." + node.getNodePath().toString() + ("".equals(node.getFilterPath().toString()) ? "" : ".") +
									node.getFilterPath().toString() + ".keyword\"" + ":\"" + node.getValue() + "\"" +*/
									termPath +
								"}" +
							"}" +
						"]" +
					"}" +
				"}" +
			"}" +
		"}";
	}
	
	private String buildFilterQuery(QueryNode node) {
		String operator;
		if (FilterNodesExpression.FilterNodesOperator.AND.equals(node.getFilterNodesExpression().getOperator())) {
			operator = "filter";
		} else if (FilterNodesExpression.FilterNodesOperator.OR.equals(node.getFilterNodesExpression().getOperator())) {
			operator = "should";
		} else {
			operator = "filter";
		}
		
		return "{" +
			"\"bool\": {" +
				"\"" + operator  + "\": [" +
					node.getFilterNodesExpression().getFilterNodes().stream().map(filterNode ->
						"{" +
							"\"term\": {" +
								"\"value." + node.getNodePath().toString() + ("".equals(filterNode.getFilterPath().toString()) ? "" : ".") +
										filterNode.getFilterPath().toString() + ".keyword\"" + ":\"" + filterNode.getValue() + "\"" +
							"}" +
						"}"
					).collect(Collectors.joining(",")) +
				"]" +
			"}" +
		"}";
	}

}
