package gr.cite.earthserver.wcs.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import gr.cite.earthserver.wcs.adapter.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryOptions;

public class WCSAdapter implements WCSAdapterAPI {
	
	private FemmeClientAPI femmeClient;
	
	/*public WCSAdapter() {
		this.femmeClient = new FemmeClient();
	}*/
	
	public WCSAdapter(String femmeUrl) {
		this.femmeClient = new FemmeClient(femmeUrl);
	}
	
	@Override
	public String insertServer(String endpoint, String name, WCSResponse server) throws ParseException, FemmeDatastoreException {
		return femmeClient.insert(WCSFemmeMapper.fromServer(endpoint, name, server));
	}

	@Override
	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeDatastoreException {
		return femmeClient.insert(WCSFemmeMapper.fromCoverage(coverage));
	}
	
	@Override
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeDatastoreException {
		return femmeClient.addToCollection(WCSFemmeMapper.fromCoverage(coverage), collectionId);
	}

	
	@Override
	public List<Server> getServers() throws FemmeDatastoreException, FemmeClientException {
		return getServers(null, null);
	}
	
	@Override
	public List<Server> getServers(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.getCollections(limit, offset).stream().map(collection -> WCSFemmeMapper.collectionToServer(collection))
			.collect(Collectors.toList());
	}
	
	@Override
	public Server getServerByEndpoint(String serverEndpoint) throws FemmeDatastoreException, FemmeClientException {
		return WCSFemmeMapper.collectionToServer(femmeClient.getCollectionByEndpoint(serverEndpoint));
	}
	
	@Override
	public Server getServerByAlias(String serverAlias) throws FemmeDatastoreException, FemmeClientException {
		return WCSFemmeMapper.collectionToServer(femmeClient.getCollectionByName(serverAlias));
	}
	
	@Override
	public <T extends Criterion> List<Server> findServers(Query<T> query, Integer limit, Integer offset, String asc,
			String desc, List<String> include, List<String> exclude, String xPath) throws FemmeDatastoreException, FemmeClientException {
		
		return femmeClient.findCollections(query, limit, offset, asc, desc, include, exclude, xPath)
				.stream().map(collection -> WCSFemmeMapper.collectionToServer(collection)).collect(Collectors.toList());
	}
	
	
	@Override
	public List<Coverage> getCoverages() throws FemmeDatastoreException, FemmeClientException {
		return getCoverages(null, null);
	}
	
	@Override
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.getDataElements(limit, offset).stream().map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
			.collect(Collectors.toList());
	}
	
	public Coverage getCoverageById(String id) throws FemmeDatastoreException {
		return WCSFemmeMapper.dataElementToCoverage(femmeClient.getDataElementById(id));
	}
	
	@Override
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, Integer limit, Integer offset,
			String asc, String desc, List<String> include, List<String> exclude, String xPath) throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.findDataElements(query, limit, offset, asc, desc, include, exclude, xPath)
				.stream().map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
				.collect(Collectors.toList());
	}
	
	
	@Override
	public List<String> getCoverageIds() throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.findDataElements(null, null, null, null, null, Arrays.asList("_id"), null, null)
			.stream().map(dataElement -> dataElement.getId()).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getCoverageIdsByServerEndpoint(List<String> serverEndpoint) throws FemmeDatastoreException, FemmeClientException {
		
		List<CriterionClient> serverEndpointCriteria = serverEndpoint.stream()
			.map(collectionEndpoint -> CriterionBuilderClient.root().eq("endpoint", collectionEndpoint).end())
			.collect(Collectors.toList());
		
		return femmeClient.findDataElements(
				QueryClient.query().addCriterion(CriterionBuilderClient.root().or(serverEndpointCriteria).end()),
				null, null, null, null, Arrays.asList("_id"), null, null)
				.stream().map(dataElement -> dataElement.getId()).collect(Collectors.toList());
	}

	@Override
	public List<String> getCoverageIdsByServerEndpoint(List<String> serverEndpoint, String xPath) throws FemmeDatastoreException, FemmeClientException {
		
		List<CriterionClient> serverEndpointCriteria = serverEndpoint.stream()
				.map(collectionEndpoint -> CriterionBuilderClient.root().eq("endpoint", collectionEndpoint).end())
				.collect(Collectors.toList());
			
			return femmeClient.findDataElements(
					QueryClient.query().addCriterion(CriterionBuilderClient.root().or(serverEndpointCriteria).end()),
					null, null, null, null, Arrays.asList("_id"), null, xPath)
					.stream().map(dataElement -> dataElement.getId()).collect(Collectors.toList());
	}

	@Override
	public List<String> getCoverageIdsByServerAlias(List<String> serverAliases) throws FemmeDatastoreException, FemmeClientException {
		
		List<CriterionClient> serverNameCriteria = serverAliases.stream()
				.map(collectionName -> CriterionBuilderClient.root().eq("name", collectionName).end())
				.collect(Collectors.toList());
			
		return femmeClient.findDataElements(
				QueryClient.query().addCriterion(CriterionBuilderClient.root().or(serverNameCriteria).end()),
				null, null, null, null, Arrays.asList("_id"), null, null)
				.stream().map(dataElement -> dataElement.getId()).collect(Collectors.toList());
			
	}
	
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.getDataElementsByName(coverageId).stream()
				.map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<Coverage> getCoveragesInServerByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException {
		return getCoveragesInServerByEndpoint(endpoint, null, null);
	}
	
	@Override
	public List<Coverage> getCoveragesInServerByEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException {
		return femmeClient.getDataElementsInCollectionByEndpoint(endpoint, limit, offset).stream().map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
			.collect(Collectors.toList());
	}

	@Override
	public Coverage getCoverageByCoverageIdInServerByEndpoint(String serverEndpoint, String coverageId) throws FemmeDatastoreException, FemmeClientException {
		
		CriterionClient collectionCriterion = CriterionBuilderClient.root().inAnyCollection(Arrays.asList(
				CriterionBuilderClient.root().eq("endpoint", serverEndpoint).end())).end();
		CriterionClient dataElementCriterion = CriterionBuilderClient.root().eq("name", coverageId).end();
		Query<? extends Criterion> query = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
		
		
		List<Coverage> coverages = findCoverages(query, 1, null, null, null, null, null, null);
		return coverages.size() > 0 ? coverages.get(0) : null;
		
	}

	@Override
	public Coverage getCoverageByCoverageIdInServerByAlias(String serverAlias, String coverageId) throws FemmeDatastoreException, FemmeClientException {
		
		CriterionClient collectionCriterion = CriterionBuilderClient.root().inAnyCollection(Arrays.asList(
				CriterionBuilderClient.root().eq("name", serverAlias).end())).end();
		CriterionClient dataElementCriterion = CriterionBuilderClient.root().eq("name", coverageId).end();
		Query<? extends Criterion> query = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
		
		
		List<Coverage> coverages = findCoverages(query, 1, null, null, null, null, null, null);
		return coverages.size() > 0 ? coverages.get(0) : null;
		
	}

	

}
