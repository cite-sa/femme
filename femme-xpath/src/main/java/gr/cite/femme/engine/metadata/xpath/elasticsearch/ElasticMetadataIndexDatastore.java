package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.utils.Pair;
import gr.cite.femme.engine.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.FilterNodesExpression;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Node;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ElasticMetadataIndexDatastore implements MetadataIndexDatastore {

	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastore.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private final StampedLock lock = new StampedLock();

	private ElasticMetadataIndexDatastoreClient client;
	private MetadataSchemaIndexDatastore schemaIndexDatastore;
	private Indices indices;

	//private AtomicBoolean reIndex = new AtomicBoolean(false);
	//private String indexName;


	public ElasticMetadataIndexDatastore(MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException, MetadataIndexException {
		this.client = new ElasticMetadataIndexDatastoreClient();
		this.schemaIndexDatastore = schemaIndexDatastore;
		this.indices = new Indices(this.client.getIndicesByAlias(this.client.getIndexAlias()));
	}

	public ElasticMetadataIndexDatastore(String hostName, int port, String indexAlias, MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException, MetadataIndexException {
		this.client = new ElasticMetadataIndexDatastoreClient(hostName, port, indexAlias);
		this.schemaIndexDatastore = schemaIndexDatastore;
		this.indices = new Indices(this.client.getIndicesByAlias(this.client.getIndexAlias()));
	}

	synchronized Indices getIndices() {
		return indices;
	}

	synchronized void setIndices(Indices indices) {
		this.indices = indices;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	public ReIndexingProcess retrieveReIndexer(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore) {
		return new ElasticReindexingProcess(metadataSchemaIndexDatastore, this, this.client);
	}

	public void insert(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		String uniqueMetadataIndexName;
		String metadataSchemaId = metadataSchema.getId();
		String randomId = UUID.randomUUID().toString();

		synchronized (this) {
			if (this.indices.compareAndAdd(metadataSchemaId, randomId)) {
				uniqueMetadataIndexName = this.client.getFullIndexName(metadataSchemaId, randomId);
				this.client.createIndex(uniqueMetadataIndexName);
				this.client.createMapping(metadataSchema, uniqueMetadataIndexName);
				this.client.createIndexAliasAssociation(uniqueMetadataIndexName);
			} else {
				//uniqueMetadataIndexName = this.client.findIndex(metadataIndexName);
				uniqueMetadataIndexName = this.client.getFullIndexName(metadataSchemaId, this.indices.getUniqueId(metadataSchemaId));
			}
		}

		/*if (!this.client.indexExists(timestampedMetadataIndexName)) {
			this.client.createIndex(timestampedMetadataIndexName);
			this.client.createIndexAliasAssociation(timestampedMetadataIndexName);
		}*/

		/*if (!this.client.mappingExists(timestampedMetadataIndexName)) {
			this.client.createMapping(metadataSchema, timestampedMetadataIndexName);
		}*/

		/*if (!this.client.indexExists(metadataIndexName)) {
			this.client.createIndex(metadataIndexName);
			this.client.createIndexAliasAssociation(metadataIndexName);
		}*/

		String indexableMetadatumSerialized;
		try {
			indexableMetadatumSerialized = mapper.writeValueAsString(indexableMetadatum);
		} catch (JsonProcessingException e) {
			throw new MetadataIndexException("Metadatum serialization failed", e);
		}
		this.client.insert(indexableMetadatumSerialized, uniqueMetadataIndexName);
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

		this.client.search(fieldsAndValues).forEach(hit -> {
			try {
				this.client.delete(hit.getId(), hit.getIndex());
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
		if (!this.client.isIndexAliasCreated()) {
			return new ArrayList<>();
		}

		ElasticScrollQuery scrollQuery = new ElasticScrollQuery(this.client);
		List<IndexableMetadatum> results = new ArrayList<>();
		try {
			//logger.info("ElasticSearch query: " + buildElasticSearchQuery(queryTree, lazy));
			//String searchQuery = buildElasticSearchQuery(queryTree, lazy);
			
			long start, end;
			start = System.currentTimeMillis();
			List<ElasticSearchQuery> searchQueries = buildElasticSearchQuery(elementIds, queryTree);
			end = System.currentTimeMillis();
			logger.info("Elasticsearch query build time: " + (end - start) + " ms");

			for (ElasticSearchQuery searchQuery: searchQueries) {
				for (Map.Entry<String, List<String>> searchQueryEntry: searchQuery.getIndicesPerQuery().entrySet()) {
					start = System.currentTimeMillis();
					scrollQuery.query(searchQueryEntry.getKey(), searchQuery.getIncludes(), searchQueryEntry.getValue(), lazy);
					while (scrollQuery.hasNext()) {
						results.addAll(scrollQuery.next());
					}
					end = System.currentTimeMillis();
					logger.info("Elasticsearch scroll query total time: " + (end - start) + " ms");
				}
			}
		} catch (Throwable e) {
			throw new MetadataIndexException("ElasticSearch scroll query failed", e);
		}

		logger.debug("Total metadata found: " + results.size());

		return results;
	}

	private List<ElasticSearchQuery> buildElasticSearchQuery(List<String> elementIds, Tree<QueryNode> queryTree) {
		List<ElasticSearchQuery> shoulds = buildShoulds(queryTree.getRoot());

		String elementIdsFilter = !elementIds.isEmpty() ?
				elementIds.stream().map(elementId -> "\"" + elementId +"\"")
						.collect(Collectors.joining(",", "{\"terms\":{\"elementId\":[", "]}},"))
				: "";
		
		ElasticSearchQuery finalQuery = new ElasticSearchQuery();
		
		shoulds.stream().forEach(query -> {
			//ElasticSearchQuery finalQuery = new ElasticSearchQuery();
			
			finalQuery.getIncludes().addAll(query.getIncludes());
			
			query.getIndicesPerQuery().forEach((subQuery, indices) -> {
				if (subQuery != null && ! subQuery.isEmpty()) {
					finalQuery.addQuery("\"query\":{" +
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
						"}", indices);
				}
			});
			
		});
		
		return Collections.singletonList(finalQuery);
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
		if (!nodeData.getFilterPath().toString().trim().isEmpty() || nodeData.getValue() != null || nodeData.getFilterNodesExpression().getFilterNodes().size() > 0) {
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
