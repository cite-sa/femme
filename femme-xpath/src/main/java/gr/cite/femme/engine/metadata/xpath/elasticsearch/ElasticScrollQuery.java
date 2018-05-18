package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticScrollQuery implements Iterator<List<IndexableMetadatum>> {

	private static final Logger logger = LoggerFactory.getLogger(ElasticScrollQuery.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private boolean firstScroll = true;
	private ElasticMetadataIndexDatastoreClient client;
	private String scrollId;
	private Response indexResponse;
	private List<IndexableMetadatum> results;
	
	public ElasticScrollQuery() {
	
	}
	public ElasticScrollQuery(ElasticMetadataIndexDatastoreClient client) {
		this.client = client;
	}

	public void query(String query, Set<String> originalIncludes, List<String> metadataSchemaIds, boolean payloadLazy) throws Throwable {
		Set<String> includes = originalIncludes.stream().map(include -> "\"value." + include + "\"").collect(Collectors.toSet());
		
		if (includes.size() > 0) {
			includes.addAll(Stream.of("metadataSchemaId", "metadatumId", "elementId", "originalContentType").map(include -> "\"" + include + "\"").collect(Collectors.toSet()));
		} else {
			payloadLazy = true;
		}

		String scrollQuery = buildScrollQuery(query, includes, payloadLazy);

		logger.info("Scroll query: " + scrollQuery);
		logger.info("Scroll query URL: " + "/" + metadataSchemaIds.stream().map(metadataSchemaId -> this.client.getIndexPrefix(metadataSchemaId) + "_*").collect(Collectors.joining(",")) + "/_search?scroll=15s");

		submitScrollQuery(scrollQuery, metadataSchemaIds);
		getResults();
		
		for (IndexableMetadatum result: this.results) {
			if (includes.size() > 0) {
				result.setValue(queryJsonTree(
					originalIncludes.iterator().next(), result.getValue(), originalIncludes.iterator().next().endsWith("#text") || originalIncludes.iterator().next().contains("@"))
				);
			}
		}
	}
	
	private String buildScrollQuery(String query, Set<String> includes, boolean payloadLazy) {
		return "{" +
			"\"_source\": {" +
				(includes.size() > 0 ? "\"includes\":[" + includes.stream().collect(Collectors.joining(",")) + "]" : "") +
				(includes.size() > 0 && payloadLazy ? "," : "") +
				(payloadLazy ? "\"excludes\":[\"value\"]" : "") +
			"}," +
			(query != null ? query + "," : "") +
			"\"sort\" : [\"_doc\"]," +
			"\"size\":  1000" +
		"}";
	}
	
	private void submitScrollQuery(String scrollQuery, List<String> metadataSchemaIds) throws IOException {
		HttpEntity entity = new NStringEntity(scrollQuery, ContentType.APPLICATION_JSON);
		this.indexResponse = this.client.get().performRequest(
			"GET",
			//"/" + this.client.getIndexAlias() + "/_search?scroll=15s",
			"/" + metadataSchemaIds.stream().map(metadataSchemaId -> this.client.getIndexPrefix(metadataSchemaId) + "_*").collect(Collectors.joining(",")) + "/_search?scroll=15s",
			Collections.emptyMap(),
			entity);
	}

	private String queryJsonTree(String query, String json, boolean getText) throws Throwable {
		if (json != null) {
			JsonNode root = mapper.readTree(json);
			String[] nodes = query.split("\\.");
			String finalNode = nodes[nodes.length - 1];
			
			List<JsonNode> results = new LinkedList<>(Collections.singletonList(root));
			boolean finalNodeIsArray = extractJsonNodes(nodes, finalNode, results);
			
			if (getText) {
				return serializeFinalNodeText(results);
			} else {
				if (finalNodeIsArray) {
					return serializeFinalArrayNodeToXml(finalNode, results);
				} else {
					return serializeFinalNodeToXml(finalNode, results);
				}
			}
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
	
	private String serializeFinalNodeText(List<JsonNode> results) {
		return results.stream().map(JsonNode::textValue).collect(Collectors.joining("\n "));
	}
	
	private String serializeFinalArrayNodeToXml(String finalNode, List<JsonNode> results) throws IOException, XMLStreamException {
		return XmlJsonConverter.jsonToXml(
			"{\"" + finalNode + "\":[" + results.stream().map(JsonNode::toString).collect(Collectors.joining("\n")) + "]}"
		);
	}
	
	private String serializeFinalNodeToXml(String finalNode, List<JsonNode> results) throws Throwable {
		try {
			return results.stream().map(jsonNode -> {
				try {
					return XmlJsonConverter.jsonToXml("{\"" + finalNode + "\":" + jsonNode.toString() + "}");
				} catch (IOException | XMLStreamException e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}).collect(Collectors.joining("\n"));
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
					"\"scroll\": \"60s\"," +
					"\"scroll_id\": \"" + this.scrollId + "\"" +
					"}";
			HttpEntity entity = new NStringEntity(scrollQuery, ContentType.APPLICATION_JSON);
			try {
				this.indexResponse = this.client.get().performRequest(
						"GET",
						"/_search/scroll",
						Collections.emptyMap(),
						entity);
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
			content = mapper.readValue(IOUtils.toString(this.indexResponse.getEntity().getContent(),
					Charset.defaultCharset()), ElasticResponseContent.class);
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

	public static void main(String[] args) throws Throwable {
		ElasticScrollQuery query = new ElasticScrollQuery();
		String res = query.queryJsonTree(
				"wcs:CoverageDescriptions.wcs:CoverageDescription.domainSet.gmlrgrid:ReferenceableGridByVectors.gmlrgrid:generalGridAxis",
				"{\"wcs:CoverageDescriptions\":{\"wcs:CoverageDescription\":{\"domainSet\":{\"gmlrgrid:ReferenceableGridByVectors\":{\"gmlrgrid:generalGridAxis\":[{\"gmlrgrid:GeneralGridAxis\":{\"gmlrgrid:sequenceRule\":{\"@\":{\"axisOrder\":\"+1\"},\"#text\":\"Linear\"}}},{\"gmlrgrid:GeneralGridAxis\":{\"gmlrgrid:sequenceRule\":{\"@\":{\"axisOrder\":\"+1\"},\"#text\":\"Linear\"}}},{\"gmlrgrid:GeneralGridAxis\":{\"gmlrgrid:sequenceRule\":{\"@\":{\"axisOrder\":\"+1\"},\"#text\":\"Linear\"}}}]}}}}}",
				false
		);
		System.out.println(res);
	}
}


