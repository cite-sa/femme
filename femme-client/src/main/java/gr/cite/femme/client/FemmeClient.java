package gr.cite.femme.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;
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
import gr.cite.femme.query.api.QueryOptions;

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
		return findCollections(null, limit, offset, null, null, null, null, null);
	}
	
	@Override
	public List<Collection> getCollections(
			Integer limit, Integer offset,
			String asc, String desc,
			List<String> include, List<String> exclude,
			String xPath) throws FemmeDatastoreException, FemmeClientException {
		return findCollections(null, limit, offset, asc, desc, include, exclude, xPath);
	}
	
	@Override
	public <T extends Criterion> List<Collection> findCollections(
			Query<T> query,
			Integer limit, Integer offset,
			String asc, String desc,
			List<String> include, List<String> exclude,
			String xPath) throws FemmeDatastoreException, FemmeClientException {
		
		String queryJson = null;
		try {
			queryJson = URLEncoder.encode(mapper.writeValueAsString(query), "UTF-8");
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException("Error in query", e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException("Error in query", e);
		}

		FemmeResponse<CollectionList> response = webTarget.path("collections")
				.queryParam("query", queryJson)
				.queryParam("limit", limit).queryParam("offset", offset)
				.queryParam("asc", asc).queryParam("desc", desc)
				.queryParam("include", include).queryParam("exclude", exclude)
				.queryParam("xpath", xPath)
				.request().get(new GenericType<FemmeResponse<CollectionList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody().getCollections();
	}
	
	@Override
	public Collection getCollectionById(String id) throws FemmeDatastoreException {
		
		FemmeResponse<Collection> response = webTarget
				.path("collections").path(id)
				.request().get(new GenericType<FemmeResponse<Collection>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody();
	}
	
	@Override
	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		List<Collection> collections = findCollections(query, 1, null, null, null, null, null, null);
		return collections.size() > 0 ? collections.get(0) : null;
	}

	@Override
	public Collection getCollectionByName(String name) throws FemmeDatastoreException, FemmeClientException {
		
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		List<Collection> collections = findCollections(query, 1, null, null, null, null, null, null);
		return collections.size() > 0 ? collections.get(0) : null;
	}
	
	
	@Override
	public List<DataElement> getDataElements() throws FemmeDatastoreException, FemmeClientException {
		return getDataElements(null, null);
	}
	
	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return findDataElements(null, limit, offset, null, null, null, null, null);
	}
	
	@Override
	public List<DataElement> getDataElements(
			Integer limit, Integer offset,
			String asc, String desc,
			List<String> include, List<String> exclude,
			String xPath) throws FemmeDatastoreException, FemmeClientException {
		return findDataElements(null, limit, offset, asc, desc, include, exclude, xPath);
	}
	
	@Override
	public <T extends Criterion> List<DataElement> findDataElements(
			Query<T> query,
			Integer limit, Integer offset,
			String asc, String desc,
			List<String> include, List<String> exclude,
			String xPath)
			throws FemmeDatastoreException, FemmeClientException {
		
		String queryJson = null;
		try {
			queryJson = URLEncoder.encode(mapper.writeValueAsString(query), "UTF-8");
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException("Error in query", e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException("Error in query", e);
		}
		
		FemmeResponse<DataElementList> response = webTarget.path("dataElements")
				.queryParam("query", queryJson)
				.queryParam("limit", limit).queryParam("offset", offset)
				.queryParam("asc", asc).queryParam("desc", desc)
				.queryParam("include", include).queryParam("exclude", exclude)
				.queryParam("xpath", xPath)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody().getDataElements();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeDatastoreException {
		return getDataElementsInCollectionById(collectionId, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset) throws FemmeDatastoreException {
		
		FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(collectionId).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody().getDataElements();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		return getDataElementsInCollectionByEndpoint(endpoint, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		
		CriterionClient collectionEndpointCriterion = CriterionBuilderClient.root().eq("endpoint", endpoint).end();
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().inCollections(Arrays.asList(collectionEndpointCriterion)).end());
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}*/
		
		/*return response.getEntity().getBody().getDataElements();*/
		return findDataElements(query, limit, offset, null, null, null, null, null);
	}
	
	public List<DataElement> getDataElementsByIdInCollectionById(String collectionid, String dataElementId) throws FemmeDatastoreException, FemmeClientException {
		
		FemmeResponse<DataElement> response = webTarget
				.path("collections").path(collectionid)
				.path("dataElements").path(dataElementId)
				.request().get(new GenericType<FemmeResponse<DataElement>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody().getDataElements();
	}

	@Override
	public DataElement getDataElementById(String id) throws FemmeDatastoreException {
		FemmeResponse<DataElement> response = webTarget
				.path("dataElements")
				.path(id)
				.request().get(new GenericType<FemmeResponse<DataElement>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeDatastoreException(response.getMessage());
		}
		
		return response.getEntity().getBody();
	}
	
	@Override
	public DataElement getDataElementByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		List<DataElement> dataElements = findDataElements(query, 1, null, null, null, null, null, null);
		return dataElements.size() > 0 ? dataElements.get(0) : null; 
	}

	@Override
	public List<DataElement> getDataElementsByName(String name) throws FemmeDatastoreException, FemmeClientException {
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		return findDataElements(query, null, null, null, null, null, null, null);
	}
	
}
