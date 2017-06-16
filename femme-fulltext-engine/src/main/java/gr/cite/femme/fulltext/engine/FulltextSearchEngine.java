package gr.cite.femme.fulltext.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticFulltextSearchClient;
import gr.cite.femme.fulltext.engine.elasticsearch.FulltextSearchException;

import java.net.UnknownHostException;

public class FulltextSearchEngine {
	private static final ObjectMapper mapper = new ObjectMapper();

	private ElasticFulltextSearchClient indexClient;

	public FulltextSearchEngine(String host, int port) throws UnknownHostException {
		this.indexClient = new ElasticFulltextSearchClient(host, port);
	}

	public FulltextSearchEngine(String host, int port, String indexName) throws UnknownHostException {
		this.indexClient = new ElasticFulltextSearchClient(host, port, indexName);
	}

	public void insert(FulltextDocument doc) throws FulltextSearchException {
		String jsonDoc;

		try {
			jsonDoc = mapper.writeValueAsString(doc);
		} catch (JsonProcessingException e) {
			throw new FulltextSearchException(e.getMessage(), e);
		}

		this.indexClient.insert(jsonDoc);
	}

	public void delete(String id) throws FulltextSearchException {
		this.indexClient.delete(id);
	}

	public String deleteByElementId(String elementId) {
		return "";
	}

	public FulltextDocument search(FulltextDocument searchDoc) {
		return null;
	}
}
