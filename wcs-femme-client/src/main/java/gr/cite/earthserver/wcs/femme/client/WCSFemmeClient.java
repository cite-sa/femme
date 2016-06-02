package gr.cite.earthserver.wcs.femme.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.earthserver.wcs.client.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSParseUtils;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Metadatum;

public class WCSFemmeClient implements FemmeClient {
	private static final Logger logger = LoggerFactory.getLogger(WCSFemmeClient.class);
	
	private final static String FEMME_URL = "http://localhost:8081/femme-application/femme/";
	
	private Client client;
	
	private WebTarget webTarget;
	
	
	public WCSFemmeClient() {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(FEMME_URL);
	}
	
	public static Collection toCollection(WCSResponse response) throws ParseException {
		Collection collection = new Collection();
		
		collection.setEndpoint(response.getEndpoint());
		collection.setName(WCSParseUtils.getServerName(response.getResponse()));
		
		collection.getMetadata().add(toMetadatum(response));
		
		return collection;
	}
	
	public static DataElement toDataElement(WCSResponse response) throws ParseException {
		DataElement dataElement = new DataElement();
		
		dataElement.setName(WCSParseUtils.getCoverageId(response.getResponse()));
		dataElement.setEndpoint(response.getEndpoint());
		
		dataElement.getMetadata().add(toMetadatum(response));
		
		return dataElement;
	}
	
	public static Metadatum toMetadatum(WCSResponse response) {
		Metadatum metadatum = new Metadatum();
		metadatum.setContentType(response.getContentType().toString());
		metadatum.setValue(response.getResponse());
		return metadatum;
	}


	@Override
	public String insert(DataElement dataElement) {
		// TODO Auto-generated method stub
		return null;
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
