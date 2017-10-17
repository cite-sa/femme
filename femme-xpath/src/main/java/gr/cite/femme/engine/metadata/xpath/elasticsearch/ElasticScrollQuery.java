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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

	public ElasticScrollQuery(ElasticMetadataIndexDatastoreClient client) {
		this.client = client;
	}

	public void query(String query, Set<String> originalIncludes, List<String> metadataSchemaIds, boolean payloadLazy) throws IOException, XMLStreamException {
		Set<String> includes = originalIncludes.stream().map(include -> "\"value." + include + "\"").collect(Collectors.toSet());
		//Set<String> includes = payloadLazy ? new HashSet<>() : originalIncludes.stream().map(include -> "\"value." + include + "\"").collect(Collectors.toSet());
		if (includes.size() > 0) {
			includes.addAll(Stream.of("metadataSchemaId", "metadatumId", "elementId", "originalContentType").map(include -> "\"" + include + "\"").collect(Collectors.toSet()));
		} else {
			payloadLazy = true;
		}

		String scrollQuery = "{" +
				"\"_source\": {" +
					(includes.size() > 0 ? "\"includes\":[" + includes.stream().collect(Collectors.joining(",")) + "]" : "") +
					(includes.size() > 0 && payloadLazy ? "," : "") +
					(payloadLazy ? "\"excludes\":[\"value\"]" : "") +
				"}," +
				(query != null ? query + "," : "") +
				"\"sort\" : [\"_doc\"]," +
				"\"size\":  1000" +
			"}";

		logger.info("Scroll query: " + scrollQuery);
		logger.info("Scroll query URL: " + "/" + metadataSchemaIds.stream().map(metadataSchemaId -> this.client.getIndexPrefix(metadataSchemaId) + "_*").collect(Collectors.joining(",")) + "/_search?scroll=15s");

		HttpEntity entity = new NStringEntity(scrollQuery, ContentType.APPLICATION_JSON);
		this.indexResponse = this.client.get().performRequest(
					"GET",
					//"/" + this.client.getIndexAlias() + "/_search?scroll=15s",
				"/" + metadataSchemaIds.stream().map(metadataSchemaId -> this.client.getIndexPrefix(metadataSchemaId) + "_*").collect(Collectors.joining(",")) + "/_search?scroll=15s",
					Collections.emptyMap(),
					entity);
		getResults();
		for (IndexableMetadatum result: this.results) {
			if (includes.size() > 0) {
				result.setValue(
						ElasticScrollQuery.queryJsonTree(
								originalIncludes.iterator().next(),
								result.getValue(),
								originalIncludes.iterator().next().endsWith("#text") || originalIncludes.iterator().next().contains("@"))
				);
			}
		}
	}

	private static String queryJsonTree(String query, String json, boolean getText) throws IOException, XMLStreamException {
		if (json != null) {
			JsonNode root = mapper.readTree(json);
			String[] nodes = query.split("\\.");
			String finalNode = nodes[nodes.length - 1];
			query = "/" + query.replace(".", "/");
			JsonNode result = root.at(query);
			if (getText) {
				return result.textValue();
			} else {
				//return result.toString();
				return XmlJsonConverter.jsonToXml("{\"" + finalNode + "\":" + result.toString() + "}");
			}
		} else {
			return "";
		}
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
				logger.error("ElasticSearch scroll query failed", e);
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

	public static void main(String[] args) throws IOException, XMLStreamException {
		String res = ElasticScrollQuery.queryJsonTree(
				"wcs:CoverageDescriptions.wcs:CoverageDescription.wcs:CoverageId",
				"{\"wcs:CoverageDescriptions\":{\"wcs:CoverageDescription\":{\"wcs:CoverageId\":{\"#text\": \"ecfire_fire_weather_index\"}}}}",
				false
		);
		System.out.println(res);
	}
}


