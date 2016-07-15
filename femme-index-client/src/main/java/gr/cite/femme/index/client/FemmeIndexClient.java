package gr.cite.femme.index.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


import gr.cite.femme.client.api.MetadataIndexClient;
import gr.cite.femme.model.Metadatum;

public class FemmeIndexClient implements MetadataIndexClient {

	Client client;
	
	WebTarget webTarget;
	
	public FemmeIndexClient(String femmeIndexHost) {
		client = ClientBuilder.newClient()/*.register(JacksonFeature.class)*/;
		webTarget = client.target(femmeIndexHost);
	}
	
	public void ping() {
		String response = webTarget.path("ping").request().get(String.class);
		System.out.println(response);
	}
	
	public void index(Metadatum metadatum) {
		webTarget.path("index").request().post(Entity.entity(metadatum, MediaType.APPLICATION_JSON));
	}

	public void reIndex(Metadatum metadatum) {
		// TODO Auto-generated method stub
		
	}

	public void reIndex() {
		// TODO Auto-generated method stub
		
	}

	public String search(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		FemmeIndexClient client = new FemmeIndexClient("http://localhost:8083/femme-index-application/index");
		client.ping();
	}
	
}
