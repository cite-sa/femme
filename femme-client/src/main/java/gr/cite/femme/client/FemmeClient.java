package gr.cite.femme.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.core.dto.ImportEndpoint;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.core.dto.CollectionList;
import gr.cite.femme.core.dto.DataElementList;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.model.Collection;

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
	public String beginImport(String endpointAlias, String endpoint) throws FemmeException {
		Response response = webTarget.path("importer").path("imports")
				.request().post(Entity.entity(new ImportEndpoint(endpointAlias, endpoint), MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug("Import " + femmeResponse.getEntity().getBody() + " for endpoint " + endpoint + " has been successfully created");
		return femmeResponse.getEntity().getBody();
	}

	@Override
	public void endImport(String importId) throws FemmeException {
		Response response = webTarget.path("importer").path("imports").path(importId).request().delete();

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug(femmeResponse.getMessage());
	}

	@Override
	public String importCollection(String importId, Collection collection) throws FemmeException {
		Response response = webTarget.path("importer").path("imports").path(importId).path("collections")
				.request().post(Entity.entity(collection, MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != 201) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug("Collection " + femmeResponse.getEntity().getHref() + " has been successfully inserted");
		return femmeResponse.getEntity().getBody();
	}

	@Override
	public String importInCollection(String importId, DataElement dataElement) throws FemmeException {
		Response response = webTarget.path("importer").path("imports").path(importId).path("dataElements")
				.request().post(Entity.entity(dataElement, MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != 201) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug("DataElement " + femmeResponse.getEntity().getHref() + " has been successfully inserted");
		return femmeResponse.getEntity().getBody();
	}

	@Override
	public String insert(Collection collection) throws FemmeException {
		Response response = webTarget.path("admin").path("collections")
				.request().post(Entity.entity(collection, MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != 201) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug("Collection " + femmeResponse.getEntity().getHref() + " has been successfully inserted");
		
		return femmeResponse.getEntity().getBody();
	}
	
	@Override
	public String insert(DataElement dataElement) throws FemmeException {
		
		Response response = webTarget.path("admin").path("dataElements")
				.request().post(Entity.entity(dataElement, MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != 201) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		logger.debug("DataElement " + femmeResponse.getEntity().getHref() + " has been successfully inserted");
		
		return femmeResponse.getEntity().getBody();
	}

	@Override
	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeException {
		Response response = webTarget.path("admin")
				.path("collections").path(collectionId).path("dataElements")
				.request().post(Entity.entity(dataElement, MediaType.APPLICATION_JSON));

		FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
		if (response.getStatus() != 201) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		logger.debug("DataElement " + femmeResponse.getEntity().getHref() + " has been successfully inserted in collection " + collectionId);
		
		return femmeResponse.getEntity().getBody();
	}
	
	
	@Override
	public List<Collection> getCollections() throws FemmeException, FemmeClientException {
		return getCollections(null, null);
	}

	@Override
	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		return getCollections(limit, offset, null);
	}
	
	@Override
	public List<Collection> getCollections(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return findCollections(null, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), xPath);
	}
	
	@Override
	public <T extends Criterion> List<Collection> findCollections(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException {
		
		String queryJson = null, optionsJson = null;
		try {
			queryJson = mapper.writeValueAsString(query);
			optionsJson = mapper.writeValueAsString(options);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException(e.getMessage(), e);
		}

		Response response = webTarget.path("collections")
				.queryParam("query", UriComponent.encode(queryJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("options", UriComponent.encode(optionsJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
				.queryParam("xpath", xPath)
				.request().get();

		FemmeResponse<CollectionList> femmeResponse = response.readEntity(new GenericType<FemmeResponse<CollectionList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getCollections();
	}
	
	@Override
	public Collection getCollectionById(String id) throws FemmeException {
		Response response = webTarget
				.path("collections").path(id)
				.request().get();

		FemmeResponse<Collection> femmeResponse = response.readEntity(new GenericType<FemmeResponse<Collection>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody();
	}
	
	@Override
	public Collection getCollectionByEndpoint(String endpoint) throws FemmeException, FemmeClientException {
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("endpoint", endpoint).end());
		
		List<Collection> collections = findCollections(query, QueryOptionsMessenger.builder().limit(1).build(), null);
		return collections.size() > 0 ? collections.get(0) : null;
	}

	@Override
	public Collection getCollectionByName(String name) throws FemmeException, FemmeClientException {
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("name", name).end());
		
		List<Collection> collections = findCollections(query, QueryOptionsMessenger.builder().limit(1).build(), null);
		return collections.size() > 0 ? collections.get(0) : null;
	}
	
	
	@Override
	public List<DataElement> getDataElements() throws FemmeException, FemmeClientException {
		return getDataElements(null, null);
	}
	
	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		return getDataElements(limit, offset, null);
	}
	
	@Override
	public List<DataElement> getDataElements(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return findDataElements(null, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), xPath);
	}

	public List<DataElement> getDataElements(Integer limit, Integer offset, List<String> includes, List<String> excludes, String xPath) throws FemmeException, FemmeClientException {
		return findDataElements(null,
				QueryOptionsMessenger.builder()
						.limit(limit).offset(offset)
						.include(new HashSet<>(includes))
						.exclude(new HashSet<>(excludes))
						.build(),
				xPath);
	}

	public List<DataElement> getDataElementsInMemoryXPath(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return findDataElements(null, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), xPath, true);
	}

	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException {
		return findDataElements(query, options, xPath, false);
	}

	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath, boolean inMemoryXPath)
			throws FemmeException, FemmeClientException {
		
		String queryJson = null, optionsJson = null;
		try {
			if (query != null) {
				queryJson = mapper.writeValueAsString(query);
			}
			if (options != null) {
				optionsJson = mapper.writeValueAsString(options);
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeClientException(e.getMessage(), e);
		}
		
		//webTarget = webTarget.path("dataElements").path(inMemoryXPath ? "xpath" : "");
		/*if (inMemoryXPath) {
			webTarget = webTarget.path("xpath");
		}*/

		WebTarget target = webTarget.path("dataElements").path(inMemoryXPath ? "xpath" : "");
		if (queryJson != null) {
			target = target.queryParam("query", UriComponent.encode(queryJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
		}
		if (optionsJson != null) {
			target = target.queryParam("options", UriComponent.encode(optionsJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
		}
		if (xPath != null) {
			target = target.queryParam("xpath", xPath);
		}

		logger.debug("FeMME request [" + target.getUri() + "]");
		logger.debug("FeMME request [" + target.toString() + "]");

		Response response = target.request().get(Response.class);

		FemmeResponse<DataElementList> femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
		
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeException, FemmeClientException {
		Response response = webTarget
				.path("collections").path(collectionId)
				.path("dataElements")
				.request().get();

		FemmeResponse<DataElementList> femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		QueryClient collectionQuery = QueryClient.query().addCriterion(CriterionBuilderClient.root().inCollections(
				Collections.singletonList(CriterionBuilderClient.root().eq("_id", collectionId).end())).end());
		
		return findDataElements(collectionQuery, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeException, FemmeClientException {
		return getDataElementsInCollectionByEndpoint(endpoint, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		
		CriterionClient collectionEndpointCriterion = CriterionBuilderClient.root().eq("endpoint", endpoint).end();
		QueryClient collectionQuery = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().inAnyCollection(Arrays.asList(collectionEndpointCriterion)).end());
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeException(response.getMessage());
		}*/
		
		/*return response.getEntity().getBody().getDataElements();*/
		return findDataElements(collectionQuery, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), null);
	}
	
	@Override
	public DataElement getDataElementById(String id) throws FemmeException {
		Response response = webTarget
				.path("dataElements")
				.path(id)
				.request().get();

		FemmeResponse<DataElement> femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElement>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody();
	}

	@Override
	public DataElement getDataElementById(String id, String xPath) throws FemmeException {
		Response response = webTarget
				.path("dataElements")
				.path(id)
				.queryParam("xpath", xPath)
				.request().get();

		FemmeResponse<DataElement> femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElement>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}

		return femmeResponse.getEntity().getBody();
	}

	@Override
	public List<DataElement> getDataElementsByName(String name) throws FemmeException, FemmeClientException {
		return findDataElements(QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("name", name).end()), null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByName(String name) throws FemmeException, FemmeClientException {
		return getDataElementsInCollectionByName(name, null, null);
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionByName(String name, Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		
		CriterionClient collectionNameCriterion = CriterionBuilderClient.root().eq("name", name).end();
		QueryClient collectionQuery = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().inAnyCollection(Arrays.asList(collectionNameCriterion)).end());
		
		/*FemmeResponse<DataElementList> response = webTarget
				.path("collections").path(endpoint).path("dataElements")
				.queryParam("limit", limit).queryParam("offset", offset)
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});
		
		if (response.getStatus() != 200) {
			logger.error(response.getMessage());
			throw new FemmeException(response.getMessage());
		}*/
		
		/*return response.getEntity().getBody().getDataElements();*/
		return findDataElements(collectionQuery, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), null);
	}
	
	public List<DataElement> getDataElementsByIdInCollectionById(String collectionid, String dataElementId) throws FemmeException, FemmeClientException {
		Response response = webTarget
				.path("collections").path(collectionid)
				.path("dataElements").path(dataElementId)
				.request().get();

		FemmeResponse<DataElementList>femmeResponse = response.readEntity(new GenericType<FemmeResponse<DataElementList>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getDataElements();
	}
	
}
