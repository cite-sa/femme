package gr.cite.femme.metadata.xpath.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

public class ElasticMetadataIndexDatastoreClient {

	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastoreClient.class);
	
	private static final String ELASTICSEARCH_HOST_NAME = "localhost";
	private static final int ELASTICSEARCH_PORT = 9200;
	private static final String ELASTICSEARCH_INDEX_NAME = "metadataindex";

	private String indexName;

	private RestClient client;

	public ElasticMetadataIndexDatastoreClient() throws UnknownHostException {
		this(ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_HOST_NAME, ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_PORT);
	}

	public ElasticMetadataIndexDatastoreClient(String hostName, int port) throws UnknownHostException {
		/*client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), port));*/
		this.indexName = ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_INDEX_NAME;
		client = RestClient.builder(new HttpHost(hostName, port, "http")).build();
		try {
			Response indexExistenceResponse = client.performRequest("HEAD", "/" + ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_INDEX_NAME);
			if (indexExistenceResponse.getStatusLine().getStatusCode() == 404) {
				client.performRequest("PUT", "/" + ElasticMetadataIndexDatastoreClient.ELASTICSEARCH_INDEX_NAME);
			}
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

	public String getIndexName() {
		return indexName;
	}
}
