package gr.cite.femme.metadata.xpath.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.datastores.MetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.ElasticResponseContent;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.ElasticResponseHit;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ElasticMetadataIndexDatastore implements MetadataIndexDatastore {

	private static final Logger logger = LoggerFactory.getLogger(ElasticMetadataIndexDatastore.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	private ElasticMetadataIndexDatastoreClient client;

	public ElasticMetadataIndexDatastore() throws UnknownHostException {
		this.client = new ElasticMetadataIndexDatastoreClient();
	}

	public ElasticMetadataIndexDatastore(ElasticMetadataIndexDatastoreClient client) {
		this.client = client;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public void indexMetadatum(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException {
		/*try {
			client.get().prepareIndex("metadata", "jsonMetadatum")
					.setSource(mapper.writeValueAsBytes(indexableMetadatum));
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Error during indexable metadata serialization", e);
		}*/

		/*try {
			System.out.println(mapper.writeValueAsString(indexableMetadatum));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}*/
		Response mapping = null;
		try {
			mapping = client.get().performRequest(
					"GET",
					"/metadataindex/_mapping/" + "metadataschema_" + indexableMetadatum.getMetadataSchemaId());
		} catch (ResponseException e) {
			if (e.getResponse().getStatusLine().getStatusCode() == 404) {
				createMapping(metadataSchema);
			} else {
				logger.error(e.getMessage(), e);
				throw new MetadataIndexException("Mapping retrieval failed", e);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Mapping retrieval failed", e);
		}

		try {
			Map<String, Object> mappingEntity = mapper.readValue(
					IOUtils.toString(mapping.getEntity().getContent(),
							Charset.forName("UTF-8")), new TypeReference<Map<String, Object>>(){});
			if (mappingEntity.size() == 0) {
				createMapping(metadataSchema);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		HttpEntity entity;
		try {
			entity = new NStringEntity(mapper.writeValueAsString(indexableMetadatum), ContentType.APPLICATION_JSON);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Error during indexable metadata serialization", e);
		}



		try {
			client.get().performRequest(
					"POST",
					"/metadataindex/metadataschema_" + indexableMetadatum.getMetadataSchemaId(),
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Indexing failed", e);
		}
	}

	@Override
	public List<IndexableMetadatum> query(String query) throws MetadataIndexException {
		String finalQuery = "{" +
				"\"query\": {" +
					"\"term\" : {" +
						query +
					"}" +
				"}" +
			"}";
		HttpEntity entity = new NStringEntity(finalQuery, ContentType.APPLICATION_JSON);

		Response indexResponse = null;
		try {
			 indexResponse = client.get().performRequest(
					"POST",
					"/metadataindex/jsonMetadatum/_search",
					Collections.emptyMap(),
					entity);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Metadata index query error", e);
		}

		ElasticResponseContent content = null;
		try {
			content = mapper.readValue(IOUtils.toString(indexResponse.getEntity().getContent(),
					Charset.defaultCharset()), ElasticResponseContent.class);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataIndexException("Metadata index query serialization error", e);
		}

		List<IndexableMetadatum> results = content.getHits().getHits().stream().map(ElasticResponseHit::getSource).collect(Collectors.toList());

		return results;
	}

	private void createMapping(MetadataSchema schema) {
		List<String> nestedValueMappings = schema.getSchema().stream().filter(JSONPath::isArray).map(jsonPath ->
				"{" +
					"\"properties\": {" +
						"\"value." + jsonPath.getPath() + "\": {" +
							"\"type\": \"nested\"" +
						"}" +
					"}" +
				"}"
		).collect(Collectors.toList());

		for (String nestedValueMapping: nestedValueMappings) {
			HttpEntity entity = new NStringEntity(nestedValueMapping, ContentType.APPLICATION_JSON);
			try {
				Response indexResponse = client.get().performRequest(
						"PUT",
						"/metadataindex/_mapping/metadataschema_" + schema.getId(),
						Collections.emptyMap(),
						entity);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}
}
