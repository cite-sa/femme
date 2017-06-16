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
		Map<String, String> metadatumIdAndValue = new HashMap<>();
		metadatumIdAndValue.put("metadatumId", metadatumId);
		this.client.deleteByQuery(metadatumIdAndValue);
	}

	@Override
	public void delete(String field, String value) throws MetadataIndexException {
		Map<String, String> metadatumIdAndValue = new HashMap<>();
		metadatumIdAndValue.put(field, value);
		this.client.deleteByQuery(metadatumIdAndValue);
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
			List<ElasticSearchQuery> searchQueries = buildElasticSearchQuery(elementIds, queryTree);

			for (ElasticSearchQuery searchQuery: searchQueries) {
				for (Map.Entry<String, List<String>> searchQueryEntry: searchQuery.getIndicesPerQuery().entrySet()) {
					scrollQuery.query(searchQueryEntry.getKey(), searchQuery.getIncludes(), searchQueryEntry.getValue(), lazy);
					while (scrollQuery.hasNext()) {
						results.addAll(scrollQuery.next());
					}
				}
			}
		} catch (IOException | XMLStreamException e) {
			throw new MetadataIndexException("ElasticSearch scroll query failed", e);
		}

		logger.debug("Total metadadata found: " + results.size());

		return results;
	}

	private List<ElasticSearchQuery> buildElasticSearchQuery(List<String> elementIds, Tree<QueryNode> queryTree) {
		List<ElasticSearchQuery> finalQueries = new ArrayList<>();
		List<ElasticSearchQuery> shoulds = buildShoulds(queryTree.getRoot());

		String elementIdsFilter = !elementIds.isEmpty()
			? elementIds.stream().map(elementId -> "\"" + elementId +"\"")
				.collect(Collectors.joining(",", "{\"terms\":{\"elementId\":[", "]}},"))
			: "";

		shoulds.forEach(query -> {
			ElasticSearchQuery finalQuery = new ElasticSearchQuery();
			finalQuery.getIncludes().addAll(query.getIncludes());
			query.getIndicesPerQuery().forEach((key, value) ->
				finalQuery.addQuery("\"query\":{" +
						"\"bool\":{" +
							"\"filter\":[" +
								elementIdsFilter +
								"{" +
									"\"bool\":{" +
										"\"should\":[" +
											key +
										"]" +
									"}" +
								"}" +
							"]" +
						"}" +
					"}", value)
			);
			finalQueries.add(finalQuery);
		});

		return finalQueries;

		/*return shoulds.stream().map(
				should -> should.stream().collect(Collectors.joining(
					",",
					"{\"bool\":{\"must\":[",
					"]}}"))
			).collect(Collectors.joining(
				",",
				"\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[",
				"]}}}}"));*/
	}

	private List<ElasticSearchQuery> buildShoulds(Node<QueryNode> root) {
		List<ElasticSearchQuery> shoulds = new ArrayList<>();
		Set<String> includes = new HashSet<>();
		buildShould(root, shoulds, includes);
		//shoulds.forEach(should -> should.setIncludes(includes));
		return shoulds;
	}

	/*private ElasticSearchQuery buildShould(Node<QueryNode> node, List<ElasticSearchQuery> shoulds) {
		//if (node.getChildren().size() > 0) {
		for (Node<QueryNode> child : node.getChildren()) {
			//List<ElasticSearchQuery> nodeShould = new ArrayList<>(should);
			//ElasticSearchQuery childQuery = buildShould(child, new ArrayList<>(), shoulds);
			ElasticSearchQuery childQuery = buildShould(child, shoulds);
			// if (node.getData() != null && (!node.getData().getFilterPath().toString().trim().isEmpty() || node.getData().getValue() != null)) {
			if (childQuery != null) {
				shoulds.add(childQuery);
			}

			*//*QueryNode data = child.getData();
			if (!data.getFilterPath().toString().trim().isEmpty() || data.getValue() != null) {
				List<ElasticSearchQuery> subQueries = buildTermOrNested(data);
				nodeShould.forEach();
				subQueries
				nodeShould.addAll(buildTermOrNested(data));
			}*//*
		}
		//} else {
		if (node.getChildren().size() == 0) {
			//shoulds.add(should);

			if (!node.getData().getFilterPath().toString().trim().isEmpty() || node.getData().getValue() != null) {
				return buildTermOrNested(node.getData());
			}
		}

		return null;
	}*/

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

		if (!node.getData().getFilterPath().toString().trim().isEmpty() || node.getData().getValue() != null) {
			ElasticSearchQuery query = buildTermOrNested(node.getData());
			return query;
		//} else if (node.getData().isFilterPayload()) {
		} else if (node.isLeaf()) {
			//includes.add(node.getData().getNodePath().toString());
			ElasticSearchQuery query = new ElasticSearchQuery();
			query.getIncludes().add(node.getData().getNodePath().toString());
			Map<String, List<String>> indices = new HashMap<>();
			indices.put("", node.getData().getMetadataSchemaIds());
			query.setIndicesPerQuery(indices);
			return query;
		}

		return null;
	}

	private ElasticSearchQuery buildTermOrNested(QueryNode node) {
		//String subQuery;

		//List<MetadataSchema> test = this.schemaIndexDatastore.findArrayMetadataIndexPathsByRegex("^" + node.getNodePath());

		/*List<String> nestedNodes = this.schemaIndexDatastore.findArrayMetadataIndexPaths().stream()
				.map(schema -> schema.getSchema().stream().map(JSONPath::getPath).collect(Collectors.toList()))
				.flatMap(List::stream).filter(nestedPath -> node.getNodePath().toString().startsWith(nestedPath))
				.sorted(Comparator.comparing(String::length)).collect(Collectors.toList());*/

		List<String> nodeMetadataSchemaIds = new ArrayList<>(node.getMetadataSchemaIds());

		Map<String, List<String>> map = new HashMap<>();
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
				});
				/*.map(schema -> schema.getSchema().stream().map(JSONPath::getPath).collect(Collectors.toList()))
				.flatMap(List::stream).filter(nestedPath -> node.getNodePath().toString().startsWith(nestedPath))
				.sorted(Comparator.comparing(String::length)).collect(Collectors.toList());*/


		//Map<String, List<String>> finalMap = new HashMap<>();
		//List<ElasticSearchQuery> result;
		ElasticSearchQuery query = new ElasticSearchQuery();

		map.forEach((key, value) -> {
			//query.setIndices(entry.getValue());
			query.addQuery("{" +
					"\"nested\" : {" +
						"\"path\": \"value." + key + "\"," +
						"\"query\": {" +
							"\"bool\": {" +
								"\"must\": [" +
								"{" +
									"\"term\": {" +
										"\"value." + node.getNodePath().toString() + ("".equals(node.getFilterPath().toString()) ? "" : ".") +
										node.getFilterPath().toString() + ".keyword\"" + ":\"" + node.getValue() + "\"" +
									"}" +
								"}" +
							"]" +
						"}" +
					"}" +
				"}" +
			"}", value);

			/*finalMap.put("{" +
				"\"nested\" : {" +
					"\"path\": \"value." + path + "\"," +
					"\"query\": {" +
						"\"bool\": {" +
							"\"must\": [" +
								"{" +
									"\"term\": {" +
										"\"value." + node.getNodePath().toString() +
											("".equals(node.getFilterPath().toString()) ? "" : ".") +
											node.getFilterPath().toString() +
											".keyword\"" + ":\"" +
												node.getValue() + "\"" +
										"}" +
									"}" +
								"]" +
							"}" +
						"}" +
					"}" +
				"}", ids)*/
		});
			//ElasticSearchQuery query = new ElasticSearchQuery();

			//query.setIndices(node.getMetadataSchemaIds());
		query.addQuery("{" +
				"\"term\":{" +
					"\"value." + node.getNodePath().toString() + ("".equals(node.getFilterPath().toString()) ? "" : ".") +
						node.getFilterPath().toString() + ".keyword\"" + ":\"" + node.getValue() + "\"" +
				"}" +
			"}", nodeMetadataSchemaIds);
		if (node.isFilterPayload()) {
			query.getIncludes().add(node.getNodePath().toString());
		}

			/*result = new ArrayList<>();
			result.add(query);*/

			/*finalMap.put(
				"{" +
					"\"term\":{" +
						"\"value." + node.getNodePath().toString() +
							("".equals(node.getFilterPath().toString()) ? "" : ".") +
							node.getFilterPath().toString() + ".keyword\"" + ":\"" + node.getValue() + "\"" +
						"}" +
					"}", node.getMetadataSchemaIds());*/
		//}

		/*finalMap.entrySet().stream().map(entry -> {
			ElasticSearchQuery query = new ElasticSearchQuery();
			query.addQuery(entry.getKey());
			query.setIndices(entry.getValue());
			return query;
		}).collect(Collectors.toList());*/

		return query;


		/*if (nestedNodes.size() > 0) {
			subQuery = "{" +
					"\"nested\" : {" +
						"\"path\": \"value." + nestedNodes.get(0) + "\"," +
							"\"query\": {" +
								"\"bool\": {" +
									"\"must\": [" +
										"{" +
											"\"term\": {" +
												"\"value." + node.getNodePath().toString() +
												("".equals(node.getFilterPath().toString()) ? "" : ".") +
												node.getFilterPath().toString() +
												".keyword\"" + ":\"" +
												node.getValue() + "\"" +
											"}" +
										"}" +
									"]" +
								"}" +
							"}" +
						"}" +
					"}";
		} else {
			subQuery = "{" +
					"\"term\":{" +
						"\"value." + node.getNodePath().toString() +
						("".equals(node.getFilterPath().toString()) ? "" : ".") +
						node.getFilterPath().toString() + ".keyword\"" + ":\"" + node.getValue() + "\"" +
					"}" +
				"}";
		}
		return subQuery;*/
	}
}
