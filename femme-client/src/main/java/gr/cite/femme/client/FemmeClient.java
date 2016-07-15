package gr.cite.femme.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public class FemmeClient implements FemmeClientAPI {
	private static final Logger logger = LoggerFactory.getLogger(FemmeClient.class);
	
	private final static String FEMME_URL = "http://localhost:8081/femme-application/femme/";
	
	private Client client;
	
	private WebTarget webTarget;
	
	
	public FemmeClient() {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(FEMME_URL);
	}
	
	public FemmeClient(String femmeUrl) {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(femmeUrl);
	}
	
	@Override
	public String insert(DataElement dataElement) {
		
		String insertedDataElementId = webTarget
				.path("dataElements")
				.request()
				.post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), String.class);
		
		logger.debug("Collection " + insertedDataElementId + " has been successfully inserted");
		
		return insertedDataElementId;
	}

	@Override
	public String insert(Collection collection) {
		/*Entity<Collection> collectionEntity = Entity.entity(collection, MediaType.APPLICATION_JSON);*/
		
		String insertedCollectionId = webTarget
				.path("collections")
				.request()
				.post(Entity.entity(collection, MediaType.APPLICATION_JSON), String.class);
		
		logger.debug("Collection " + insertedCollectionId + " has been successfully inserted");
		
		return insertedCollectionId;

	}

	@Override
	public String addToCollection(DataElement dataElement, String collectionId) {
		String insertedDataElementId = webTarget
				.path("collections").path(collectionId).path("dataElements")
				.request()
				.post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), String.class);
		
		logger.debug("DataElement " + insertedDataElementId + " has been successfully inserted in collection " + collectionId);
		
		return insertedDataElementId;
	}
	
}
