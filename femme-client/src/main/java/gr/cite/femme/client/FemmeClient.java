package gr.cite.femme.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.core.dto.ElementList;
import gr.cite.femme.core.dto.ImportEndpoint;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.QueryClient;
//import gr.cite.femme.core.dto.CollectionList;
//import gr.cite.femme.core.dto.DataElementList;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
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
	private boolean indexModeOn = true;
	
	
	public FemmeClient() {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(FEMME_URL);
	}
	
	public FemmeClient(String femmeUrl) {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(femmeUrl);
	}
	
	public FemmeClient(String femmeUrl, boolean indexModeOn) {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target(femmeUrl);
		this.indexModeOn = indexModeOn;
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
		if (response.getStatus() != 200 && response.getStatus() != 201) {
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
		if (response.getStatus() != 200 && response.getStatus() != 201) {
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
		if (response.getStatus() != 200 && response.getStatus() != 201) {
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
		if (response.getStatus() != 200 && response.getStatus() != 201) {
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
		if (response.getStatus() != 200 && response.getStatus() != 201) {
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

		FemmeResponse<ElementList<Collection>> femmeResponse = response.readEntity(new GenericType<FemmeResponse<ElementList<Collection>>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getElements();
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
		QueryOptionsMessenger.Builder queryOptionsBuilder = QueryOptionsMessenger.builder();
		if (limit != null) {
			queryOptionsBuilder.limit(limit);
		}
		if (offset != null) {
			queryOptionsBuilder.offset(offset);
		}
		if (includes != null) {
			queryOptionsBuilder.include(new HashSet<>(includes));
		}
		if (excludes != null) {
			queryOptionsBuilder.exclude(new HashSet<>(excludes));
		}
		return findDataElements(null, queryOptionsBuilder.build(), xPath);
	}

	public List<DataElement> getDataElementsInMemoryXPath(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return getDataElementsInMemoryXPath(limit, offset, null, null, xPath);
	}
	
	public List<DataElement> getDataElementsInMemoryXPath(Integer limit, Integer offset, List<String> includes, List<String> excludes, String xPath) throws FemmeException, FemmeClientException {
		QueryOptionsMessenger.Builder queryOptionsBuilder = QueryOptionsMessenger.builder();
		if (limit != null) {
			queryOptionsBuilder.limit(limit);
		}
		if (offset != null) {
			queryOptionsBuilder.offset(offset);
		}
		if (includes != null) {
			queryOptionsBuilder.include(new HashSet<>(includes));
		}
		if (excludes != null) {
			queryOptionsBuilder.exclude(new HashSet<>(excludes));
		}
		return findDataElements(null, queryOptionsBuilder.build(), xPath, true);
	}

	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException {
		return findDataElements(query, options, xPath, false);
	}

	@Override
	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath, boolean inMemoryXPath) throws FemmeException, FemmeClientException {
		
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

		FemmeResponse<ElementList<DataElement>> femmeResponse = response.readEntity(new GenericType<FemmeResponse<ElementList<DataElement>>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return new ArrayList<>();
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getElements();
		
	}
	
	@Override
	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeException, FemmeClientException {
		Response response = webTarget
				.path("collections").path(collectionId)
				.path("dataElements")
				.request().get();

		FemmeResponse<ElementList<DataElement>> femmeResponse = response.readEntity(new GenericType<FemmeResponse<ElementList<DataElement>>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getElements();
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
		
		/*return response.getEntity().getBody().getElements();*/
		return findDataElements(collectionQuery, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), null);
	}
	
	@Override
	public DataElement getDataElementById(String id) throws FemmeException {
		return getDataElementByIdWithOptions(id, null, null);
	}

	@Override
	public DataElement getDataElementById(String id, Set<String> includes, Set<String> excludes) throws FemmeException {
		QueryOptionsMessenger options = QueryOptionsMessenger.builder().include(includes).exclude(excludes).build();
		return getDataElementByIdWithOptions(id, null, options);
	}

	@Override
	public DataElement getDataElementById(String id, String xPath) throws FemmeException {
		return getDataElementByIdWithOptions(id, xPath, null);
	}

	@Override
	public DataElement getDataElementById(String id, String xPath, Set<String> includes, Set<String> excludes) throws FemmeException {
		QueryOptionsMessenger options = QueryOptionsMessenger.builder().include(includes).exclude(excludes).build();
		return getDataElementByIdWithOptions(id, xPath, options);
	}

	private DataElement getDataElementByIdWithOptions(String id, String xPath, QueryOptionsMessenger options) throws FemmeException {
		WebTarget tempWebTarget = this.webTarget.path("dataElements").path(id);

		if (xPath != null) {
			tempWebTarget = tempWebTarget.queryParam("xpath", xPath);
		}
		if (options != null) {
			tempWebTarget = tempWebTarget.queryParam("options", options);
		}

		Response response = tempWebTarget.request().get();

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
	
	public DataElement xPathDataElementWithName(String name, String xPath) throws FemmeException, FemmeClientException {
		WebTarget tempWebTarget = this.webTarget.path("dataElements").path("name").path(name);
		
		if (xPath != null) {
			tempWebTarget = tempWebTarget.queryParam("xpath", xPath);
		}
		
		Response response = tempWebTarget.request().get();
		
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
	public DataElement xPathInMemoryDataElementWithName(String name, String xPath) throws FemmeException, FemmeClientException {
		WebTarget tempWebTarget = this.webTarget.path("dataElements").path("name").path(name).path("xpath");
		
		if (xPath != null) {
			tempWebTarget = tempWebTarget.queryParam("xpath", xPath);
		}
		
		Response response = tempWebTarget.request().get();
		
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
		
		/*return response.getEntity().getBody().getElements();*/
		return findDataElements(collectionQuery, QueryOptionsMessenger.builder().limit(limit).offset(offset).build(), null);
	}
	
	public List<DataElement> getDataElementsByIdInCollectionById(String collectionid, String dataElementId) throws FemmeException, FemmeClientException {
		Response response = webTarget
				.path("collections").path(collectionid)
				.path("dataElements").path(dataElementId)
				.request().get();

		FemmeResponse<ElementList<DataElement>> femmeResponse = response.readEntity(new GenericType<FemmeResponse<ElementList<DataElement>>>(){});
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			logger.debug(femmeResponse.getMessage());
			return null;
		} else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.error(femmeResponse.getMessage());
			throw new FemmeException(femmeResponse.getMessage());
		}
		
		return femmeResponse.getEntity().getBody().getElements();
	}
	
	public static void main(String[] args) throws FemmeException, FemmeClientException {
		final String COVERAGE_METADATA_XPATH = "/gmlcov:ReferenceableGridCoverage/gmlcov:metadata/text()";
		
		final String ENVELOPE_AXIS_LABELS_XPATH = "/gmlcov:ReferenceableGridCoverage/boundedBy/Envelope/@axisLabels";
		final String ENVELOPE_UPPER_CORNER_XPATH = "/gmlcov:ReferenceableGridCoverage/boundedBy/Envelope]/upperCorner]/text()";
		
		final String RANGE_PARAMETERS_XPATH = "/gmlcov:ReferenceableGridCoverage/gml:rangeSet/gml:rangeParameters/text()";
		
		final String ORIGIN_POINT_AXIS_LABELS_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gml:origin/Point/@axisLabels";
		final String ORIGIN_POINT_POS_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gml:origin/Point/pos/text()";
		
		final String AXIS_COEFFICIENTS_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gmlrgrid:generalGridAxis/gmlrgrid:GeneralGridAxis[gmlrgrid:gridAxesSpanned='reftime']/gmlrgrid:coefficients/text()";
		final String AXIS_OFFSET_VECTOR_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gmlrgrid:generalGridAxis/gmlrgrid:GeneralGridAxis[gmlrgrid:gridAxesSpanned='reftime']/gmlrgrid:offsetVector/text()";
		
		final String GENERAL_GRID_AXIS_LABEL_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gmlrgrid:generalGridAxis/gmlrgrid:GeneralGridAxis/gmlrgrid:gridAxesSpanned/text()";
		final String GENERAL_GRID_AXIS_COEFFICIENTS_XPATH = "/gmlcov:ReferenceableGridCoverage/domainSet/gml:ReferenceableGridByVectors/gmlrgrid:generalGridAxis/gmlrgrid:GeneralGridAxis[gmlrgrid:gridAxesSpanned='reftime']/gmlrgrid:coefficients/text()";
		
		
		FemmeClient client = new FemmeClient("http://localhost:8080/femme");
		/*String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gmlcov:ReferenceableGridCoverage xmlns=\"http://www.opengis.net/gml/3.2\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\" xmlns:swe=\"http://www.opengis.net/swe/2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wcs=\"http://www.opengis.net/wcs/2.0\" xmlns:gmlrgrid=\"http://www.opengis.net/gml/3.3/rgrid\" gml:id=\"oper_an_sfc_2t_integration\"><gmlcov:metadata>{\"Type of level short\": \"sfc\", \"MARS type short\": \"an\", \"slices\": [], \"MARS stream\": \"oper\", \"Grib1_Parameter_name\": \"2_metre_temperature\", \"Short name\": \"2t\", \"Times\": \"[0000/0600/1200/1800]\", \"MARS type long\": \"analysis\", \"Type of level long\": \"surface\", \"GRIB_table_version\": \"167.128\", \"Originating_or_generating_Center\": \"European Centre for Medium-Range Weather Forecasts\"}</gmlcov:metadata><boundedBy><Envelope srsName=\"http://earthserver2d.ecmwf.int:8080/def/crs-compound?1=http://earthserver2d.ecmwf.int:8080/def/crs/EPSG/0/4326&amp;2=http://earthserver2d.ecmwf.int:8080/def/crs/OGC/0/AnsiDate?axis-label=&#34;reftime&#34;\" axisLabels=\"Lat Long reftime\" uomLabels=\"  http://www.opengis.net/def/uom/UCUM/0/d\" srsDimension=\"3\"><lowerCorner>-90.25 -180.25 \"1979-01-01T00:00:00+00:00\"</lowerCorner><upperCorner>90.25 179.75 \"2016-12-31T18:00:00+00:00\"</upperCorner></Envelope></boundedBy><domainSet><gml:ReferenceableGridByVectors dimension=\"3\" gml:id=\"grid\"><limits><GridEnvelope><low>0 0 0</low><high>54056 719 360</high></GridEnvelope></limits><axisLabels>reftime Long Lat</axisLabels><gml:origin><Point gml:id=\"origin\" srsName=\"http://earthserver2d.ecmwf.int:8080/def/crs-compound?1=http://earthserver2d.ecmwf.int:8080/def/crs/EPSG/0/4326&amp;2=http://earthserver2d.ecmwf.int:8080/def/crs/OGC/0/AnsiDate?axis-label=&#34;reftime&#34;\" axisLabels=\"Lat Long reftime\" uomLabels=\"  http://www.opengis.net/def/uom/UCUM/0/d\" srsDimension=\"3\"><pos>90.00 -180.00 \"1979-01-01T00:00:00+00:00\"</pos></Point></gml:origin><gmlrgrid:generalGridAxis><gmlrgrid:GeneralGridAxis><gmlrgrid:offsetVector srsName=\"http://earthserver2d.ecmwf.int:8080/def/crs-compound?1=http://earthserver2d.ecmwf.int:8080/def/crs/EPSG/0/4326&amp;2=http://earthserver2d.ecmwf.int:8080/def/crs/OGC/0/AnsiDate?axis-label=&#34;reftime&#34;\" axisLabels=\"Lat Long reftime\" uomLabels=\"  http://www.opengis.net/def/uom/UCUM/0/d\" srsDimension=\"3\">                    0 0 1                </gmlrgrid:offsetVector><gmlrgrid:coefficients>0.0 0.25 0.5 0.75 1.0 1.25 1.5 1.75 2.0 2.25 2.5 2.75 3.0 3.25 3.5 3.75 4.0 4.25 4.5 4.75 5.0 5.25 5.5 5.75 6.0 6.25 6.5 6.75 7.0 7.25 7.5 7.75 8.0 8.25 8.5 8.75 9.0 9.25 9.5 9.75 10.0 10.25 10.5 10.75 11.0 11.25 11.5 11.75 12.0 12.25 12.5 12.75 13.0 13.25 13.5 13.75 14.0 14.25 14.5 14.75 15.0 15.25 15.5 15.75 16.0 16.25 16.5 16.75 17.0 17.25 17.5 17.75 18.0 18.25 18.5 18.75 19.0 19.25 19.5 19.75 20.0 20.25 20.5 20.75 21.0 21.25 21.5 21.75 22.0 22.25 22.5 22.75 23.0 23.25 23.5 23.75 24.0 24.25 24.5 24.75 25.0 25.25 25.5 25.75 26.0 26.25 26.5 26.75 27.0 27.25 27.5 27.75 28.0 28.25 28.5 28.75 29.0 29.25 29.5 29.75 30.0 30.25 30.5 30.75 31.0 31.25 31.5 31.75 32.0 32.25 32.5 32.75 33.0 33.25 33.5 33.75 34.0 34.25 34.5 34.75 35.0 35.25 35.5 35.75 36.0 36.25 36.5 36.75 37.0 37.25 37.5 37.75 38.0 38.25 38.5 38.75 39.0 39.25 39.5 39.75 40.0 40.25 40.5 40.75 41.0 41.25 41.5 41.75 42.0 42.25 42.5 42.75 43.0 43.25 43.5 43.75 44.0 44.25 44.5 44.75 45.0 45.25 45.5 45.75 46.0 46.25 46.5 46.75 47.0 47.25 47.5 47.75 48.0 48.25 48.5 48.75 49.0 49.25 49.5 49.75 50.0 50.25 50.5 50.75 51.0 51.25 51.5 51.75 52.0 52.25 52.5 52.75 53.0 53.25 53.5 53.75 54.0 54.25 54.5 54.75 55.0 55.25 55.5 55.75 56.0 56.25 56.5 56.75 57.0 57.25 57.5 57.75 58.0 58.25 58.5 58.75 59.0 59.25 59.5 59.75 60.0 60.25 60.5 60.75 61.0 61.25 61.5 61.75 62.0 62.25 62.5 62.75 63.0 63.25 63.5 63.75 64.0 64.25 64.5 64.75 65.0</gmlrgrid:coefficients><gmlrgrid:gridAxesSpanned>reftime</gmlrgrid:gridAxesSpanned><gmlrgrid:sequenceRule axisOrder=\"+1\">None</gmlrgrid:sequenceRule></gmlrgrid:GeneralGridAxis></gmlrgrid:generalGridAxis><gmlrgrid:generalGridAxis><gmlrgrid:GeneralGridAxis><gmlrgrid:offsetVector srsName=\"http://earthserver2d.ecmwf.int:8080/def/crs-compound?1=http://earthserver2d.ecmwf.int:8080/def/crs/EPSG/0/4326&amp;2=http://earthserver2d.ecmwf.int:8080/def/crs/OGC/0/AnsiDate?axis-label=&#34;reftime&#34;\" axisLabels=\"Lat Long reftime\" uomLabels=\"  http://www.opengis.net/def/uom/UCUM/0/d\" srsDimension=\"3\">0 0.5 0</gmlrgrid:offsetVector><gmlrgrid:coefficients/><gmlrgrid:gridAxesSpanned>Long</gmlrgrid:gridAxesSpanned><gmlrgrid:sequenceRule axisOrder=\"+1\">None</gmlrgrid:sequenceRule></gmlrgrid:GeneralGridAxis></gmlrgrid:generalGridAxis><gmlrgrid:generalGridAxis><gmlrgrid:GeneralGridAxis><gmlrgrid:offsetVector srsName=\"http://earthserver2d.ecmwf.int:8080/def/crs-compound?1=http://earthserver2d.ecmwf.int:8080/def/crs/EPSG/0/4326&amp;2=http://earthserver2d.ecmwf.int:8080/def/crs/OGC/0/AnsiDate?axis-label=&#34;reftime&#34;\" axisLabels=\"Lat Long reftime\" uomLabels=\"  http://www.opengis.net/def/uom/UCUM/0/d\" srsDimension=\"3\">-0.5 0 0</gmlrgrid:offsetVector><gmlrgrid:coefficients/><gmlrgrid:gridAxesSpanned>Lat</gmlrgrid:gridAxesSpanned><gmlrgrid:sequenceRule axisOrder=\"+1\">None</gmlrgrid:sequenceRule></gmlrgrid:GeneralGridAxis></gmlrgrid:generalGridAxis></gml:ReferenceableGridByVectors></domainSet><gml:rangeSet><gml:rangeParameters>[{\"axes\": [{\"name\": \"Lat\", \"min\": \"-90.25\", \"max\": \"90.25\", \"resolution\": \"-0.5\", \"type\": \"number\", \"order\": \"2\"}, {\"name\": \"Long\", \"min\": \"-180.25\", \"max\": \"179.75\", \"resolution\": \"0.5\", \"type\": \"number\", \"order\": \"1\"}, {\"name\": \"reftime\", \"min\": \"\\\"1979-01-01T00:00:00+00:00\\\"\", \"max\": \"\\\"2016-12-31T18:00:00+00:00\\\"\", \"resolution\": \"1\", \"type\": \"date\", \"order\": \"0\"}], \"messageId\": 1}]</gml:rangeParameters><gml:File><gml:fileReference>file:///data/xwcps-mars/registration/empty.grib</gml:fileReference><gml:fileStructure>application/grib</gml:fileStructure></gml:File></gml:rangeSet><gmlcov:rangeType><swe:DataRecord><swe:field name=\"2_metre_temperature\"><swe:Quantity definition=\"\"><swe:description>ei</swe:description><swe:nilValues><swe:NilValues><swe:nilValue reason=\"Nil value represents missing values.\">9999</swe:nilValue></swe:NilValues></swe:nilValues><swe:uom code=\"\"/></swe:Quantity></swe:field></swe:DataRecord></gmlcov:rangeType></gmlcov:ReferenceableGridCoverage>";
		
		
		Collection collection = new Collection();
		collection.setName("collection");
		collection.setEndpoint("http://collection");
		
		DataElement dataElement = new DataElement();
		dataElement.setEndpoint("http://dataElement");
		dataElement.setName("dataElement");
		
		Metadatum metadatum = new Metadatum();
		metadatum.setContentType(MediaType.APPLICATION_XML);
		metadatum.setEndpoint("http://metadatum");
		metadatum.setName("metadatum");
		metadatum.setValue(xml);
		
		dataElement.setMetadata(Collections.singletonList(metadatum));
		
		String collectionId = client.insert(collection);
		collection.setId(collectionId);
		
		dataElement.setCollections(Collections.singletonList(collection));
		client.insert(dataElement);*/
		
		DataElement dataElement = client.xPathDataElementWithName("dataElement", AXIS_COEFFICIENTS_XPATH);
		System.out.println(dataElement.getMetadata().get(0).getValue());
	}
	
}
