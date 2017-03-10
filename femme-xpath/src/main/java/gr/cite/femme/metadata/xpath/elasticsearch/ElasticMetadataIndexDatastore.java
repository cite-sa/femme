package gr.cite.femme.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.datastores.api.MetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.ElasticScrollQuery;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.Node;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.Tree;
import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticMetadataIndexDatastore implements MetadataIndexDatastore {

	private static final String ELASTICSEARCH_TYPE_PREFIX = "metadataschema_";

	private static final ObjectMapper mapper = new ObjectMapper();

	private ElasticMetadataIndexDatastoreClient client;
	private MetadataSchemaIndexDatastore schemaIndexDatastore;

	public ElasticMetadataIndexDatastore(MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException {
		this.client = new ElasticMetadataIndexDatastoreClient();
		this.schemaIndexDatastore = schemaIndexDatastore;
	}

	public ElasticMetadataIndexDatastore(String hostName, int port, MetadataSchemaIndexDatastore schemaIndexDatastore) throws UnknownHostException {
		this.client = new ElasticMetadataIndexDatastoreClient(hostName, port);
		this.schemaIndexDatastore = schemaIndexDatastore;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public void indexMetadatum(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		try {
			Response mapping = client.get().performRequest(
					"HEAD",
					"/" + client.getIndexName() + "/_mapping/" + ElasticMetadataIndexDatastore.ELASTICSEARCH_TYPE_PREFIX + indexableMetadatum.getMetadataSchemaId());
			if (mapping.getStatusLine().getStatusCode() == 404) {
				createMapping(metadataSchema);
			}
		} catch (IOException e) {
			throw new MetadataIndexException("ElasticSearch mapping existence check failed", e);
		}

		HttpEntity entity;
		try {
			entity = new NStringEntity(mapper.writeValueAsString(indexableMetadatum), ContentType.APPLICATION_JSON);
		} catch (JsonProcessingException e) {
			throw new MetadataIndexException("Indexable metadatum serialization failed", e);
		}
		try {
			client.get().performRequest(
					"POST",
					"/" + client.getIndexName() + "/" + ElasticMetadataIndexDatastore.ELASTICSEARCH_TYPE_PREFIX + indexableMetadatum.getMetadataSchemaId(),
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Metadatum indexing failed", e);
		}
	}

	@Override
	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree) throws MetadataIndexException {
		return query(queryTree, true);
	}

	@Override
	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException {
		/*HttpEntity entity = new NStringEntity(buildElasticSearchQuery(queryTree, lazy), ContentType.APPLICATION_JSON);
		Response indexResponse;
		try {
			 indexResponse = client.get().performRequest(
					"POST",
					"/" + client.getIndexName() + "/_search",
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			throw new MetadataIndexException("Metadata index query failed", e);
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

	private void createMapping(MetadataSchema schema) throws MetadataIndexException {
		String dynamicTemplate = schema.getSchema().stream().filter(JSONPath::isArray)
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
			client.get().performRequest(
					"PUT",
					"/" + client.getIndexName() + "/_mapping/" + ElasticMetadataIndexDatastore.ELASTICSEARCH_TYPE_PREFIX + schema.getId(),
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			throw new MetadataIndexException("ElasticSearch dynamic template creation failed");
		}
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
