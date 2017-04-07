package gr.cite.femme.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

	public void query(String query, boolean payloadLazy) throws IOException {
		String scrollQuery = "{" +
				(payloadLazy ? "\"_source\": {\"excludes\": [ \"value\" ]}," : "") +
				query + "," +
				"\"sort\" : [\"_doc\"]," +
				"\"size\":  1000" +
			"}";
		HttpEntity entity = new NStringEntity(scrollQuery, ContentType.APPLICATION_JSON);
		this.indexResponse = this.client.get().performRequest(
					"GET",
					"/" + this.client.getIndexAlias() + "/_search?scroll=15s",
					Collections.emptyMap(),
					entity);
		getResults();
	}

	@Override
	public boolean hasNext() {
		if (!this.firstScroll) {
			String scrollQuery = "{" +
					"\"scroll\": \"15s\"," +
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
			logger.error("ElasticSearch scroll response serialization failed", e);
			this.results = new ArrayList<>();
			return;
		}
		this.scrollId = content.getScrollId();

		logger.info("ElasticSearch scroll query duration: " + content.getTook() + "ms");

		this.results = content.getHits().getHits().stream().map(hit -> {
			hit.getSource().setId(hit.getId());
			return hit.getSource();
		}).collect(Collectors.toList());
	}
}


