package gr.cite.femme.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;

public class ElasticMetadataIndexDatastoreClient {

	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastoreClient.class);
	
	private static final String ELASTICSEARCH_HOST_NAME = "localhost";
	private static final int ELASTICSEARCH_PORT = 9200;

	private RestClient client;

	public ElasticMetadataIndexDatastoreClient() throws UnknownHostException {
		this(ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_HOST_NAME, ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_PORT);
	}

	public ElasticMetadataIndexDatastoreClient(String hostName, int port) throws UnknownHostException {
		/*client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), port));*/
		client = RestClient.builder(new HttpHost(hostName, port, "http")).build();

		/*String nestedValueMapping = "{" +
			"\"mappings\": {" +
				"\"jsonMetadatum\": {" +
					"\"properties\": {" +
						"\"value.wcs:CoverageDescriptions\": {" +
							"\"type\": \"nested\"" +
						"}" +
					"}" +
				"}" +
			"}" +
		"}";*/
		//HttpEntity entity = new NStringEntity(nestedValueMapping, ContentType.APPLICATION_JSON);
		try {
			Response indexResponse = client.performRequest("PUT", "/metadataindex");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void close() throws IOException {
		client.close();
	}

	public RestClient get() {
		return client;
	}
}