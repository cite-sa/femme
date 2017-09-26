package gr.cite.femme.fulltext.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticFulltextIndexClient;
import gr.cite.femme.fulltext.engine.elasticsearch.ElasticResponseHit;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class FulltextIndexEngine {
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String INDEX_NAME = "fulltext_search";

	private ElasticFulltextIndexClient indexClient;
	private String indexName;

	public FulltextIndexEngine(String host, int port) throws UnknownHostException {
		this(host, port, FulltextIndexEngine.INDEX_NAME);
	}

	public FulltextIndexEngine(String host, int port, String indexName) throws UnknownHostException {
		this.indexClient = new ElasticFulltextIndexClient(host, port);
		this.indexName = indexName;
	}

	public void insert(FulltextDocument doc) throws FulltextIndexException {
		if (!this.indexClient.indexExists(this.indexName)) {
			String settings =  "\"settings\": {" +
					"\"analysis\": {" +
						"\"filter\": {" +
							"\"grams_filter\": {" +
								"\"type\":\"ngram\"," +
								"\"min_gram\": 2," +
								"\"max_gram\": 3" +
							"}" +
						"}," +
					"\"analyzer\": {" +
						"\"grams\": {" +
							"\"type\":\"custom\"," +
							"\"tokenizer\": \"standard\"," +
							"\"filter\": [" +
								"\"lowercase\"," +
								"\"grams_filter\"" +
							"]" +
						"}" +
					"}" +
				"}" +
			"}";
			
			String mappings = "\"mappings\" : {" +
					"\"elements\" : {" +
						"\"properties\" : {" +
							"\"name\": {" +
								"\"type\": \"text\"," +
								"\"analyzer\": \"simple\"" +
							"}," +
							"\"elementId\": {" +
								"\"type\": \"keyword\"," +
								"\"index\": \"not_analyzed\"" +
							"}," +
							"\"metadatumId\": {" +
								"\"type\": \"keyword\"," +
								"\"index\": \"not_analyzed\"" +
							"}" +
						"}" +
					"}" +
				"}";


			this.indexClient.createIndex(this.indexName, "{" + mappings + "}");
		}

		String jsonDoc;
		try {
			jsonDoc = mapper.writeValueAsString(doc);
		} catch (JsonProcessingException e) {
			throw new FulltextIndexException(e.getMessage(), e);
		}

		this.indexClient.insert(jsonDoc, this.indexName);
	}

	public void delete(String id) throws FulltextIndexException {
		this.indexClient.delete(id, this.indexName);
	}

	public void deleteByElementId(String elementId) throws FulltextIndexException {
		deleteByQuery("elementId", elementId);
	}

	public void deleteByMetadatumId(String metadatumId) throws FulltextIndexException {
		deleteByQuery("metadatumId", metadatumId);
	}

	private void deleteByQuery(String field, String value) throws FulltextIndexException {
		String deleteQuery = "{" +
				"\"query\":{" +
					"\"constant_score\":{" +
						"\"filter\":{" +
							"\"term\":{" +
								"\"" + field + "\":\"" + value + "\"" +
							"}" +
						"}" +
					"}" +
				"}" +
			"}";
		this.indexClient.deleteByQuery(deleteQuery, this.indexName);
	}

	public List<FulltextDocument> search(FulltextSearchQueryMessenger query) throws FulltextIndexException {
			return this.indexClient.search(buildElasticSearchQuery(query), this.indexName)
					.stream().map(ElasticResponseHit::getSource).collect(Collectors.toList());
	}

	private String buildElasticSearchQuery(FulltextSearchQueryMessenger query) {
		if (query.getMetadataField() != null) {
			/*return "{" +
					"\"query\": {" +
						"\"match\" : {" +
							"\"" + query.getMetadataField().getField() + "\" : {" +
								"\"query\" : \"" + query.getMetadataField().getValue() + "\"," +
								"\"fuzziness\": \"auto\"" +
							"}" +
						"}" +
					"}" +
				"}";*/

			return "{" +
				"\"query\": {" +
					"\"match_phrase_prefix\": {" +
						"\"" + query.getMetadataField().getField() + "\": \"" + query.getMetadataField().getValue() + "\"" +
					"}" +
				"}" +
			"}";
			/*return "{" +
				"\"query\": {" +
					"\"match\" : {" +
						"\"" + query.getMetadataField().getField() + "\": \"" + query.getMetadataField().getValue() + "\"" +
					"}" +
				"}" +
			"}";*/
		}
		return "";
	}

	public static void main(String[] args) {
		/*String settings =  "\"settings\": {" +
				"\"analysis\": {" +
				"\"filter\": {" +
				"\"grams_filter\": {" +
				"\"type\":\"ngram\"," +
				"\"min_gram\": 2," +
				"\"max_gram\": 3" +
				"}" +
				"}," +
				"\"analyzer\": {" +
				"\"grams\": {" +
				"\"type\":\"custom\"," +
				"\"tokenizer\": \"standard\"," +
				"\"filter\": [" +
				"\"lowercase\"," +
				"\"grams_filter\"" +
				"]" +
				"}" +
				"}" +
				"}" +
				"}";

		String mappings = "\"mappings\" : {" +
				"\"elements\" : {" +
				"\"properties\" : {" +
				"\"name\": {" +
				"\"type\": \"text\"," +
				"\"analyzer\": \"simple\"" +
				"}," +
				"\"elementId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}," +
				"\"metadatumId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}" +
				"}" +
				"}" +
				"}";*/


		String settings =  "\"settings\": {" +
				"\"analysis\": {" +
				"\"filter\": {" +
				"\"grams_filter\": {" +
				"\"type\":\"ngram\"," +
				"\"min_gram\": 2," +
				"\"max_gram\": 7" +
				"}" +
				"}," +
				"\"analyzer\": {" +
				"\"grams\": {" +
				"\"type\":\"custom\"," +
				"\"tokenizer\": \"standard\"," +
				"\"filter\": [" +
				"\"lowercase\"," +
				"\"grams_filter\"" +
				"]" +
				"}" +
				"}" +
				"}" +
				"}";

		String mappings = "\"mappings\" : {" +
				"\"elements\" : {" +
				"\"properties\" : {" +
				"\"name\": {" +
				"\"type\": \"string\"," +
				"\"analyzer\": \"grams\"" +
				"}," +
				"\"elementId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}," +
				"\"metadatumId\": {" +
				"\"type\": \"string\"," +
				"\"index\": \"not_analyzed\"" +
				"}" +
				"}" +
				"}" +
				"}";

		System.out.println("{" + settings + "," + mappings + "}");
	}
}
