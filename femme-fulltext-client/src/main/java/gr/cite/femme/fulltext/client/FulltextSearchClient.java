package gr.cite.femme.fulltext.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.fulltext.core.FulltextDocument;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class FulltextSearchClient implements FulltextSearchClientAPI {
	private static final ObjectMapper mapper = new ObjectMapper();
	private WebTarget target;

	public FulltextSearchClient(String endpoint) {
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

		Response response = this.target.path("elements").request()
				.post(Entity.entity(doc, MediaType.APPLICATION_JSON));
		System.out.println(response.readEntity(String.class));
	}

	@Override
	public void delete(String id) {
		this.target.path(id).request().delete();
	}

	@Override
	public void deleteByElementId(String elementId) {
		this.target.queryParam("elementId", elementId).request().delete();
	}

	@Override
	public void search() {
		//this.target.
	}

}
