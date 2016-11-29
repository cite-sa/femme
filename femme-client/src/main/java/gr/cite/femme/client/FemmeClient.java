package gr.cite.femme.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.dto.CollectionList;
import gr.cite.femme.dto.DataElementList;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryOptionsFields;

public class FemmeClient implements FemmeClientAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeClient.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String FEMME_URL = "http://localhost:8081/femme-application";
//	private static final String FEMME_URL = "http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT";
	
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

		FemmeResponse<String> response = webTarget.path("admin").path("collections")
				.request().post(Entity.entity(collection, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		if (response.getStatus() != 201) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		logger.debug("Collection " + response.getEntity().getHref() + " has been successfully inserted");
		
		return response.getEntity().getBody();

	}
	
	@Override
	public String insert(DataElement dataElement) throws FemmeDatastoreException {
		
		FemmeResponse<String> response = webTarget.path("admin").path("dataElements")
				.request().post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		if (response.getStatus() != 201) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		logger.debug("DataElement " + response.getEntity().getHref() + " has been successfully inserted");
		
		return response.getEntity().getBody();
	}

	@Override
	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeDatastoreException {
		
		FemmeResponse<String> response = webTarget.path("admin")
				.path("collections").path(collectionId).path("dataElements")
				.request().post(Entity.entity(dataElement, MediaType.APPLICATION_JSON), new GenericType<FemmeResponse<String>>(){});
		
		if (response.getStatus() != 201) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		logger.debug("DataElement " + response.getEntity().getHref() + " has been successfully inserted in collection " + collectionId);
		
		return response.getEntity().getBody();
	}
	
	
	@Override
	public List<Collection> getCollections() throws FemmeDatastoreException, FemmeClientException {
		return getCollections(null, null);
	}

	@Override
	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return getCollections(limit, offset, null);
	}
	
	@Override
	public List<Collection> getCollections(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException {
		return findCollections(null, QueryOptionsFields.builder().limit(limit).offset(offset).build(), xPath);
	}
	
	@Override
	public <T extends Criterion> List<Collection> findCollections(Query<T> query, QueryOptionsFields options, String xPath)
			throws FemmeDatastoreException, FemmeClientException {
		
		String queryJson = null, optionsJson = null;
		try {
			queryJson = mapper.writeValueAsString(query);
			optionsJson = mapper.writeValueAsString(options);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException(e.getMessage(), e);
		}

		/*FemmeResponse<CollectionList> response = null;*/
		Response response = null;
		FemmeResponse<CollectionList> femmeResponse = null;
		response = webTarget.path("collections")
				.queryParam("query", UriComponent.encode(queryJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("options", UriComponent.encode(optionsJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("xpath", xPath)
				.request().get(Response.class);
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<CollectionList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getCollections();
	}
	
	@Override
	public Collection getCollectionById(String id) throws FemmeDatastoreException {
		
		Response response = null;
		FemmeResponse<Collection> femmeResponse = null;

		response = webTarget
				.path("collections").path(id)
				.request().get(Response.class);
		
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<Collection>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody();
		
	}
	
	@Override
	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		List<Collection> collections = findCollections(query, QueryOptionsFields.builder().limit(1).build(), null);
		return collections.size() > 0 ? collections.get(0) : null;
	}

	@Override
	public Collection getCollectionByName(String name) throws FemmeDatastoreException, FemmeClientException {
		
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		List<Collection> collections = findCollections(query, QueryOptionsFields.builder().limit(1).build(), null);
		return collections.size() > 0 ? collections.get(0) : null;
	}
	
	
	@Override
	public List<DataElement> getDataElements() throws FemmeDatastoreException, FemmeClientException {
		return getDataElements(null, null);
	}
	
	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return getDataElements(limit, offset, null);
	}
	
	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException {
		return findDataElements(null, QueryOptionsFields.builder().limit(limit).offset(offset).build(), xPath);
	}
	
	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsFields options, String xPath)
			throws FemmeDatastoreException, FemmeClientException {
		
		String queryJson = null, optionsJson = null;
		try {
			queryJson = mapper.writeValueAsString(query);
			optionsJson = mapper.writeValueAsString(options);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException(e.getMessage(), e);
		}
		
		Response response = null;
		FemmeResponse<DataElementList> femmeResponse = null;

		response = webTarget.path("dataElements")
				.queryParam("query", UriComponent.encode(queryJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("options", UriComponent.encode(optionsJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("xpath", xPath)
				.request().get(Response.class);
		
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
		
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeDatastoreException, FemmeClientException {
		//return getDataElementsInCollectionById(collectionId, null, null);
		
		Response response = null;
		FemmeResponse<DataElementList> femmeResponse = null;
		
		response = webTarget
				.path("collections").path(collectionId)
				.path("dataElements")
				.request().get(Response.class);
		
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		
		QueryClient collectionQuery = QueryClient.query().addCriterion(CriterionBuilderClient.root().inCollections(
				Arrays.asList(CriterionBuilderClient.root().eq("_id", collectionId).end())).end());
		
		return findDataElements(collectionQuery, QueryOptionsFields.builder().limit(limit).offset(offset).build(), null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		return getDataElementsInCollectionByEndpoint(endpoint, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		
		CriterionClient collectionEndpointCriterion = CriterionBuilderClient.root().eq("endpoint", endpoint).end();
		QueryClient collectionQuery = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().inAnyCollection(Arrays.asList(collectionEndpointCriterion)).end());
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}*/
		
		/*return response.getEntity().getBody().getDataElements();*/
		return findDataElements(collectionQuery, QueryOptionsFields.builder().limit(limit).offset(offset).build(), null);
	}
	
	@Override
	public DataElement getDataElementById(String id) throws FemmeDatastoreException {
		
		Response response = null;
		FemmeResponse<DataElement> femmeResponse = null;
		
		response = webTarget
				.path("dataElements")
				.path(id)
				.request().get(Response.class);
		
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElement>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody();
		
	}

	@Override
	public List<DataElement> getDataElementsByName(String name) throws FemmeDatastoreException, FemmeClientException {
		return findDataElements(QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("name", name).end()), null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByName(String name) throws FemmeDatastoreException, FemmeClientException {
		return getDataElementsInCollectionByName(name, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByName(String name, Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		
		CriterionClient collectionNameCriterion = CriterionBuilderClient.root().eq("name", name).end();
		QueryClient collectionQuery = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().inAnyCollection(Arrays.asList(collectionNameCriterion)).end());
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}*/
		
		/*return response.getEntity().getBody().getDataElements();*/
		return findDataElements(collectionQuery, QueryOptionsFields.builder().limit(limit).offset(offset).build(), null);
	}
	
	public List<DataElement> getDataElementsByIdInCollectionById(String collectionid, String dataElementId) throws FemmeDatastoreException, FemmeClientException {
		
		Response response = null;
		FemmeResponse<DataElementList> femmeResponse = null;
		
		response = webTarget
				.path("collections").path(collectionid)
				.path("dataElements").path(dataElementId)
				.request().get(Response.class);
		
		femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeDatastoreException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
		
	}
	
}
