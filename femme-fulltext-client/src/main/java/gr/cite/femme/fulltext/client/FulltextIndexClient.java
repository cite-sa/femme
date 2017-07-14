package gr.cite.femme.fulltext.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class FulltextIndexClient implements FulltextIndexClientAPI {
	private static final ObjectMapper mapper = new ObjectMapper();
	private WebTarget target;

	public FulltextIndexClient(String endpoint) {
		this.target =  ClientBuilder.newClient().target(endpoint);
	}

	@Override
	public void insert(String elementId, String metadatumId, Map<String, Object> fields) {
		FulltextDocument doc = new FulltextDocument();
		doc.setElementId(elementId);
		doc.setMetadatumId(metadatumId);
		doc.setFulltextFields(fields);

		try {
			System.out.println(mapper.writeValueAsString(doc));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Response response = this.target.path("admin").path("elements").request()
				.post(Entity.entity(doc, MediaType.APPLICATION_JSON));
		System.out.println(response.readEntity(String.class));
	}

	@Override
	public void delete(String id) {
		this.target.path("admin").path("elements").path(id).request().delete();
	}

	@Override
	public void deleteByElementId(String elementId) {
		this.target.path("admin").path("elements").queryParam("elementId", elementId).request().delete();
	}

	@Override
	public void deleteByMetadatumId(String metadatumId) {
		this.target.path("admin").path("elements").queryParam("metadatumId", metadatumId).request().delete();
	}

	@Override
	public List<FulltextDocument> search(FulltextSearchQueryMessenger query) {
		return this.target.path("elements").request().post(Entity.entity(query, MediaType.APPLICATION_JSON)).readEntity(new GenericType<List<FulltextDocument>>() {});
	}

}
