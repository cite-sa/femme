package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.QueryBuilder;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.core.exceptions.MetadataIndexException;
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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
		this.indices = new Indices();
	}

	public ElasticMetadataIndexDatastore(String hostName, int port, String indexAlias, MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException, MetadataIndexException {
		this.client = new ElasticMetadataIndexDatastoreClient(hostName, port, indexAlias);
		this.schemaIndexDatastore = schemaIndexDatastore;
		this.indices = new Indices();
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	public ReIndexingProcess retrieveReIndexer(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore) {
		return new ElasticReindexingProcess(metadataSchemaIndexDatastore, this.client);
	}

	public void insert(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		String timestampedMetadataIndexName;

		String metadataIndexName = this.client.getIndexAlias() + "_" + metadataSchema.getId();
		if (this.indices.compareAndAdd(metadataIndexName)) {
			timestampedMetadataIndexName = metadataIndexName + "_" + UUID.randomUUID();
			this.client.createIndex(timestampedMetadataIndexName);
			this.client.createIndexAliasAssociation(timestampedMetadataIndexName);
			this.client.createMapping(metadataSchema, timestampedMetadataIndexName);
		} else {
			timestampedMetadataIndexName = this.client.findIndex(metadataIndexName);
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
		this.client.insert(indexableMetadatumSerialized, timestampedMetadataIndexName);
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
		if (!this.client.isIndexAliasCreated()) {
			return new ArrayList<>();
		}
		/*HttpEntity entity = new NStringEntity(buildElasticSearchQuery(queryTree, lazy), ContentType.APPLICATION_JSON);
		Response indexResponse;
		try {
			 indexResponse = client.get().performRequest(
					"POST",
					"/" + client.getIndexAlias() + "/_search",
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Metadata insert query failed", e);
		}

		ElasticResponseContent response;
		try {
			response = mapper.readValue(IOUtils.toString(indexResponse.getEntity().getContent(),
					Charset.defaultCharset()), ElasticResponseContent.class);
		} catch (IOException e) {
			throw new MetadataIndexException("ElasticSearch response serialization failed", e);
		}

		return response.getHits().getHits().stream().map(hit -> {
			hit.getSource().setId(hit.getId());
			return hit.getSource();
		}).collect(Collectors.toList());*/

		ElasticScrollQuery scrollQuery = new ElasticScrollQuery(this.client);
		try {
			logger.info("ElasticSearch query: " + buildElasticSearchQuery(queryTree, lazy));
			scrollQuery.query(buildElasticSearchQuery(queryTree, lazy), true);
		} catch (IOException e) {
			throw new MetadataIndexException("ElasticSearch scroll query failed", e);
		}

		List<IndexableMetadatum> results = new ArrayList<>();
		while (scrollQuery.hasNext()) {
			results.addAll(scrollQuery.next());
		}

		return results;
	}

	private String buildElasticSearchQuery(Tree<QueryNode> queryTree, boolean lazy) {
		return buildShoulds(queryTree.getRoot()).stream().map(
				should -> should.stream().collect(Collectors.joining(
					",",
					"{\"bool\":{\"must\":[",
					"]}}"))
			).collect(Collectors.joining(
				",",
				"\"query\":{\"bool\":{\"filter\":{\"bool\":{\"should\":[",
				"]}}}}"));
	}

	private List<List<String>> buildShoulds(Node<QueryNode> node) {
		List<List<String>> shoulds = new ArrayList<>();
		buildShould(node, new ArrayList<>(), shoulds);
		return shoulds;
	}

	private void buildShould(Node<QueryNode> node, List<String> should, List<List<String>> shoulds) {
		for (Node<QueryNode> child: node.getChildren()) {
			QueryNode data = child.getData();
			List<String> nodeShould = new ArrayList<>(should);
			if (!"".equals(data.getFilterPath().toString()) || data.getValue() != null) {
				nodeShould.add(buildTermOrNested(data));
			}
			buildShould(child, nodeShould, shoulds);
		}
		if (node.getChildren().size() == 0) {
			shoulds.add(should);
		}
	}

	private String buildTermOrNested(QueryNode node) {
		String subQuery;
		List<String> nestedNodes = schemaIndexDatastore.findArrayMetadataIndexPaths().stream()
				.map(schema ->  schema.getSchema().stream().map(JSONPath::getPath).collect(Collectors.toList()))
				.flatMap(List::stream).filter(nestedPath -> node.getNodePath().toString().startsWith(nestedPath)).collect(Collectors.toList());
		if (nestedNodes.size() > 0) {
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
		return subQuery;
	}
}
