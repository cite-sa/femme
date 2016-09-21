package gr.cite.femme.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.dto.CollectionList;
import gr.cite.femme.dto.DataElementList;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;

public class FemmeClient implements FemmeClientAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeClient.class);
	
//	private static final String FEMME_URL = "http://localhost:8081/femme-application";
	private static final String FEMME_URL = "http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT";
	
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
	public String insert(Collection collection) throws FemmeDatastoreException {

		FemmeResponse<String> response = webTarget.path("admin")
				.path("collections").path("collection")
				.request()
				.post(Entity.entity(collection, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		logger.debug("Collection " + response.getEntity() + " has been successfully inserted");
		
		return response.getEntity();

	}
	
	@Override
	public String insert(DataElement dataElement) throws FemmeDatastoreException {
		
		FemmeResponse<String> response = webTarget.path("admin")
				.path("dataElements").path("dataElement")
				.request()
				.post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		logger.debug("DataElement " + response.getEntity() + " has been successfully inserted");
		
		return response.getEntity();
	}

	@Override
	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeDatastoreException {
		FemmeResponse<String> response = webTarget.path("admin")
				.path("collections").path(collectionId).path("dataElements").path("dataElement")
				.request()
				.post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		logger.debug("DataElement " + response.getEntity() + " has been successfully inserted in collection " + collectionId);
		
		return response.getEntity();
	}
	
	@Override
	public List<Collection> getCollections() throws FemmeDatastoreException {
		return getCollections(null, null);
	}

	@Override
	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeDatastoreException {
		return findCollections(null, limit, offset, null);
	}
	
	@Override
	public <T extends Criterion> List<Collection> findCollections(Query<T> query, Integer limit, Integer offset, String xPath) throws FemmeDatastoreException {
		
		FemmeResponse<CollectionList> response = webTarget
				.path("collections").queryParam("limit", limit).queryParam("offset", offset).queryParam("xpath", xPath)
				.request().post(Entity.entity(query,  MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<CollectionList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getCollections();
	}
	
	@Override
	public List<DataElement> getDataElements() throws FemmeDatastoreException {
		return getDataElements(null, null);
	}

	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException {
		return findDataElements(null, limit, offset, null);
	}
	
	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, Integer limit, Integer offset, String xPath)
			throws FemmeDatastoreException {
		FemmeResponse<DataElementList> response = webTarget
				.path("dataElements").queryParam("limit", limit).queryParam("offset", offset).queryParam("xpath", xPath)
				.request()
				.post(Entity.entity(query,  MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getDataElements();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollection(String collectionId) throws FemmeDatastoreException {
		return getDataElementsInCollection(collectionId, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollection(String collectionId, Integer limit, Integer offset) throws FemmeDatastoreException {
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(collectionId).path("dataElements")
				.queryParam("limit", limit)
				.queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getDataElements();*/
		return new ArrayList<>();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionWithEndpoint(String endpoint) throws FemmeDatastoreException {
		return getDataElementsInCollectionWithEndpoint(endpoint, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionWithEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException {
		
		FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit)
				.queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getDataElements();
	}
	
	@Override
	public Collection getCollectionById(String id) throws FemmeDatastoreException {
		
		FemmeResponse<Collection> response = webTarget
				.path("collections")
				.path(id)
				.request().get(new GenericType<FemmeResponse<Collection>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity();
	}
	
	@Override
	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException {
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		FemmeResponse<Collection> response = webTarget
				.path("collections")
				.request().post(Entity.entity(query, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<Collection>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity();
	}

	@Override
	public List<Collection> getCollectionsByName(String name) throws FemmeDatastoreException {
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		FemmeResponse<CollectionList> response = webTarget
				.path("collections")
				.request().post(Entity.entity(query, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<CollectionList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getCollections();
	}
	
	@Override
	public DataElement getDataElementById(String id) throws FemmeDatastoreException {
		FemmeResponse<DataElement> response = webTarget
				.path("dataElements")
				.path(id)
				.request().get(new GenericType<FemmeResponse<DataElement>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity();
	}
	
	@Override
	public DataElement getDataElementByEndpoint(String endpoint) throws FemmeDatastoreException {
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		FemmeResponse<DataElementList> response = webTarget
				.path("dataElements")
				.request()
				.post(Entity.entity(query, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		List<DataElement> dataElements = response.getEntity().getDataElements();
		
		return dataElements.size() > 0 ? dataElements.get(0) : null; 
	}

	@Override
	public List<DataElement> getDataElementsByName(String name) throws FemmeDatastoreException {
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		FemmeResponse<DataElementList> response = webTarget
				.path("dataElements")
				.request()
				.post(Entity.entity(query, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (!response.getStatus()) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getDataElements();
	}
	
}
