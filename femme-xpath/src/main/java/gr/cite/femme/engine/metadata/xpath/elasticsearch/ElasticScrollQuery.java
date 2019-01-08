package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticScrollQuery implements Iterator<List<IndexableMetadatum>> {

	private static final Logger logger = LoggerFactory.getLogger(ElasticScrollQuery.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private boolean firstScroll = true;
	private ElasticMetadataIndexDatastoreRepository repository;
	private String scrollId;
	private Response indexResponse;
	private List<IndexableMetadatum> results;
	
	public ElasticScrollQuery(ElasticMetadataIndexDatastoreRepository repository) {
		this.repository = repository;
	}

	public void query(String query, Set<String> originalIncludes, List<String> metadataSchemaIds, boolean payloadLazy) throws Throwable {
		Set<String> includes = originalIncludes.stream().map(include -> "\"value." + include + "\"").collect(Collectors.toSet());
		
		if (includes.size() > 0) {
			includes.addAll(Stream.of("metadataSchemaId", "metadatumId", "elementId", "originalContentType").map(include -> "\"" + include + "\"").collect(Collectors.toSet()));
		} else {
			payloadLazy = true;
		}

		String scrollQuery = buildScrollQuery(query, includes, payloadLazy);

		submitScrollQuery(scrollQuery, metadataSchemaIds);
		getResults();
		
		for (IndexableMetadatum result: this.results) {
			if (includes.size() > 0) {
				result.setValue(queryJsonTree(
					originalIncludes.iterator().next(), result.getValue(),
					result.getOriginalContentType(),
					originalIncludes.iterator().next().endsWith("#text") || originalIncludes.iterator().next().contains("@")
				));
			}
		}
	}
	
	private String buildScrollQuery(String query, Set<String> includes, boolean payloadLazy) {
		return "{" +
			"\"_source\": {" +
				(includes.size() > 0 ? "\"includes\":[" + String.join(",", includes) + "]" : "") +
				(includes.size() > 0 && payloadLazy ? "," : "") +
				(payloadLazy ? "\"excludes\":[\"value\"]" : "") +
			"}," +
			(query == null || "".equals(query) ? "" : query + ",") +
			"\"sort\" : [\"_doc\"]," +
			"\"size\":  1000" +
		"}";
	}
	
	private void submitScrollQuery(String scrollQuery, List<String> metadataSchemaIds) throws IOException {
		String indices = metadataSchemaIds.stream().map(metadataSchemaId -> this.repository.getIndexPrefix(metadataSchemaId) + "_*").collect(Collectors.joining(","));
		String indicesEndpoint = indices.getBytes(StandardCharsets.UTF_8).length < 4000 ? indices : this.repository.getIndexAlias();
		
		logger.debug("Scroll endpoint: " + indicesEndpoint);
		logger.debug("Scroll query: " + scrollQuery);
		
		Request request = new Request("POST", "/" + indicesEndpoint + "/_search?scroll=15s");
		request.setJsonEntity(scrollQuery);
		
		this.indexResponse = this.repository.get().performRequest(request);
	}

	private String queryJsonTree(String query, String json, String contentType, boolean getText) throws Throwable {
		if (json != null) {
			JsonNode root = mapper.readTree(json);
			String[] nodes = query.split("\\.");
			String finalNode = nodes[nodes.length - 1];
			
			List<JsonNode> results = new LinkedList<>(Collections.singletonList(root));
			boolean finalNodeIsArray = extractJsonNodes(nodes, finalNode, results);
			
			return serializeFinalNode(finalNode, results, contentType, getText, finalNodeIsArray);
		}
		
		return "";
	}
	
	private boolean extractJsonNodes(String[] pathNodes, String finalNode, List<JsonNode> results) {
		List<JsonNode> localResults = new LinkedList<>(results);
		boolean finalNodeIsArray = false;
		List<JsonNode> intermediateResults = new LinkedList<>();
		
		for (String node: pathNodes) {
			boolean intermediateResultsArrayCreated = false;
			
			for (int i = 0; i < localResults.size(); i ++) {
				if (localResults.get(i).isArray()) {
					finalNodeIsArray = finalNode.equals(node);
					
					List<JsonNode> subResults = extractNodesFromArray(i, node, localResults);
					
					intermediateResultsArrayCreated = true;
					intermediateResults.addAll(subResults);
				} else {
					localResults.set(i, localResults.get(i).path(node));
				}
			}
			
			if (intermediateResultsArrayCreated) localResults = intermediateResults;
		}
		
		copyLocalToFinalResults(localResults, results);
		
		return finalNodeIsArray;
	}
	
	private List<JsonNode> extractNodesFromArray(int index, String pathNode, List<JsonNode> localResults) {
		List<JsonNode> subResults = new ArrayList<>();
		for (int j = 0; j < localResults.get(index).size(); j ++) {
			subResults.add(localResults.get(index).path(j).path(pathNode));
		}
		return subResults;
	}
	
	private String serializeFinalNode(String finalNode, List<JsonNode> results, String contentType, boolean getText, boolean isArray) throws Throwable {
		if (getText) {
			return serializeFinalNodeText(results);
		} else {
			if (isArray) {
				return serializeFinalArrayNode(finalNode, results, contentType);
			} else {
				return serializeFinalNode(finalNode, results, contentType);
			}
		}
	}
	
	private String serializeFinalNodeText(List<JsonNode> results) {
		return results.stream().map(JsonNode::textValue).collect(Collectors.joining("\n "));
	}
	
	private String serializeFinalArrayNode(String finalNode, List<JsonNode> results, String contentType) throws IOException, XMLStreamException {
		if (contentType.toLowerCase().contains("xml")) {
			return XmlJsonConverter.femmeJsonToXml(
				"{\"" + finalNode + "\":[" + results.stream().map(JsonNode::toString).collect(Collectors.joining("\n")) + "]}"
			);
		} else if (contentType.toLowerCase().contains("json")) {
			return XmlJsonConverter.femmeJsonToJson(
				"{\"" + finalNode + "\":[" + results.stream().map(JsonNode::toString).collect(Collectors.joining("\n")) + "]}"
			);
		} else {
			logger.error("Content type [" + contentType + "] not supported");
			return "";
		}
		
	}
	
	private String serializeFinalNode(String finalNode, List<JsonNode> results, String contentType) throws Throwable {
		try {
			return results.stream().map(jsonNode -> {
				//if (! jsonNode.isMissingNode()) {
					try {
						if (contentType.toLowerCase().contains("xml")) {
							return XmlJsonConverter.femmeJsonToXml("{\"" + finalNode + "\":" + jsonNode.toString() + "}");
						} else if (contentType.toLowerCase().contains("json")) {
							return XmlJsonConverter.femmeJsonToJson("{\"" + finalNode + "\":" + jsonNode.toString() + "}");
						} else {
							logger.error("Content type [" + contentType + "] not supported");
							return null;
						}
					} catch (IOException | XMLStreamException e) {
						logger.error(e.getMessage(), e);
						throw new RuntimeException(e);
					}
				//}
			}).filter(Objects::nonNull).collect(Collectors.joining("\n"));
		} catch (Exception e) {
			throw e.getCause();
		}
	}
	
	private void copyLocalToFinalResults(List<JsonNode> localResults, List<JsonNode> finalResults) {
		finalResults.clear();
		finalResults.addAll(localResults);
	}

	@Override
	public boolean hasNext() {
		if (!this.firstScroll) {
			String scrollQuery = "{" +
					"\"scroll\": \"30s\"," +
					"\"scroll_id\": \"" + this.scrollId + "\"" +
				"}";
			
			Request request = new Request("POST", "/_search/scroll");
			request.setJsonEntity(scrollQuery);
			
			try {
				
				this.indexResponse = this.repository.get().performRequest(request);
			} catch (IOException e) {
				logger.error("Elasticsearch scroll query failed", e);
				return false;
			}
			getResults();
		}
		return this.results.size() != 0;
	}

	@Override
	public List<IndexableMetadatum> next() {
		if (this.firstScroll) {
			this.firstScroll = false;
		}
		return this.results;
	}

	private void getResults() {
		ElasticResponseContent content;
		try {
			String contentString = IOUtils.toString(this.indexResponse.getEntity().getContent(), Charset.defaultCharset());
			content = mapper.readValue(contentString, ElasticResponseContent.class);
		} catch (IOException e) {
			logger.error("Elasticsearch scroll response serialization failed", e);
			this.results = new ArrayList<>();
			return;
		}
		this.scrollId = content.getScrollId();

		logger.info("Elasticsearch scroll query time: " + content.getTook() + " ms");

		this.results = content.getHits().getHits().stream().map(hit -> {
			hit.getSource().setId(hit.getId());
			return hit.getSource();
		}).collect(Collectors.toList());
	}
	
}


