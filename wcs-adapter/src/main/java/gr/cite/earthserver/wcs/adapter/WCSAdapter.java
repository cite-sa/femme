package gr.cite.earthserver.wcs.adapter;

import gr.cite.earthserver.wcs.adapter.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.geo.GeoRequests;
import gr.cite.earthserver.wcs.geo.GeoUtils;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import jersey.repackaged.com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WCSAdapter implements WCSAdapterAPI {
	
	private FemmeClientAPI femmeClient;
	private GeoRequests geoRequests;
	private boolean indexModeOn = true;
	
	public WCSAdapter(String femmeUrl) {
		this.femmeClient = new FemmeClient(femmeUrl);
		this.indexModeOn = true;
	}
	
	public WCSAdapter(String femmeUrl, boolean indexModeOn) {
		this.femmeClient = new FemmeClient(femmeUrl);
		this.indexModeOn = indexModeOn;
	}
	
	public WCSAdapter(String femmeUrl, String femmeGeoUrl) {
		this.femmeClient = new FemmeClient(femmeUrl);
		this.geoRequests = new GeoRequests(femmeGeoUrl);
		this.indexModeOn = true;
	}
	
	public WCSAdapter(String femmeUrl, String femmeGeoUrl, boolean indexModeOn) {
		this.femmeClient = new FemmeClient(femmeUrl);
		this.geoRequests = new GeoRequests(femmeGeoUrl);
		this.indexModeOn = indexModeOn;
	}
	
	@Override
	public String beginImport(String endpointAlias, String endpoint) throws FemmeException {
		return this.femmeClient.beginImport(endpointAlias, endpoint);
	}
	
	@Override
	public void endImport(String importId) throws FemmeException {
		this.femmeClient.endImport(importId);
	}
	
	@Override
	public String importServer(String importId, String endpoint, String name, WCSResponse server) throws ParseException, FemmeException {
		Collection collection = WCSFemmeMapper.fromServer(endpoint, name, server);
		String collectionId = this.femmeClient.importCollection(importId, collection);
		//this.geoRequests.insert(GeoUtils.convertDataToCoverageGeo(coverage, dataElement));
		return collectionId;
	}
	
	@Override
	public String importCoverage(String importId, WCSResponse coverage) throws ParseException, FemmeException {
		DataElement dataElement = WCSFemmeMapper.fromCoverage(coverage);
		
		String dataElementId = this.femmeClient.importInCollection(importId, dataElement);
		dataElement.setId(dataElementId);
		
		if (this.geoRequests != null) {
			this.geoRequests.insert(GeoUtils.convertDataToCoverageGeo(coverage, dataElement));
		}
		
		return dataElementId;
	}
	
	@Override
	public String insertServer(String endpoint, String name, WCSResponse server) throws ParseException, FemmeException {
		return this.femmeClient.insert(WCSFemmeMapper.fromServer(endpoint, name, server));
	}
	
	@Override
	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeException {
		DataElement dataElement = WCSFemmeMapper.fromCoverage(coverage);
		//this.geoRequests.insert(GeoUtils.convertDataToCoverageGeo(coverage, dataElement));
		return this.femmeClient.insert(dataElement);
	}
	
	@Override
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeException {
		DataElement dataElement = WCSFemmeMapper.fromCoverage(coverage);
		//this.geoRequests.insert(GeoUtils.convertDataToCoverageGeo(coverage, dataElement));
		return this.femmeClient.addToCollection(dataElement, collectionId);
	}
	
	
	@Override
	public List<Server> getServers() throws FemmeException, FemmeClientException {
		return getServers(null, null);
	}
	
	@Override
	public List<Server> getServers(Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		return getServers(limit, offset, null);
	}
	
	@Override
	public List<Server> getServers(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return this.femmeClient.getCollections(limit, offset, xPath).stream().map(WCSFemmeMapper::collectionToServer)
				.collect(Collectors.toList());
	}
	
	@Override
	public <T extends Criterion> List<Server> findServers(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException {
		return this.femmeClient.findCollections(query, options, xPath)
				.stream().map(WCSFemmeMapper::collectionToServer).collect(Collectors.toList());
	}
	
	@Override
	public Server getServer(String filterValue) throws FemmeException, FemmeClientException {
		Server server = WCSFemmeMapper.collectionToServer(this.femmeClient.getCollectionByName(filterValue));
		if (server == null) {
			server = WCSFemmeMapper.collectionToServer(this.femmeClient.getCollectionByEndpoint(filterValue));
		}
		return server;
	}
	
	
	@Override
	public List<Coverage> getCoverages() throws FemmeException, FemmeClientException {
		return getCoverages(null, null);
	}
	
	@Override
	public List<Coverage> getCoverages(String xPath) throws FemmeException, FemmeClientException {
		return getCoverages(new ArrayList<>(), new ArrayList<>(), xPath);
	}
	
	@Override
	public List<Coverage> getCoverages(List<String> includes, List<String> excludes, String xPath) throws FemmeException, FemmeClientException {
		if (this.indexModeOn)
			return this.femmeClient.getDataElements(null, null, includes, excludes, xPath).stream()
					.map(WCSFemmeMapper::dataElementToCoverage).collect(Collectors.toList());
		else
			return this.femmeClient.getDataElementsInMemoryXPath(null, null, includes, excludes, xPath).stream()
					.map(WCSFemmeMapper::dataElementToCoverage).collect(Collectors.toList());
	}
	
	@Override
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeException, FemmeClientException {
		return getCoverages(limit, offset, null);
	}
	
	@Override
	public List<Coverage> getCoverages(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return this.femmeClient.getDataElements(limit, offset, xPath).stream().map(WCSFemmeMapper::dataElementToCoverage)
				.collect(Collectors.toList());
	}
	
	@Override
	public Coverage getCoverageById(String id) throws FemmeException {
		return WCSFemmeMapper.dataElementToCoverage(this.femmeClient.getDataElementById(id));
	}
	
	@Override
	public Coverage getCoverageById(String id, Set<String> includes, Set<String> excludes) throws FemmeException {
		return WCSFemmeMapper.dataElementToCoverage(this.femmeClient.getDataElementById(id, includes, excludes));
	}
	
	@Override
	public Coverage getCoverageById(String id, String xPath) throws FemmeException {
		DataElement dataElement = this.femmeClient.getDataElementById(id, xPath);
		return WCSFemmeMapper.dataElementToCoverage(dataElement);
	}
	
	@Override
	public Coverage getCoverageById(String id, String xPath, Set<String> includes, Set<String> excludes) throws FemmeException {
		DataElement dataElement = this.femmeClient.getDataElementById(id, xPath, includes, excludes);
		return WCSFemmeMapper.dataElementToCoverage(dataElement);
	}
	
	@Override
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException {
		return this.femmeClient.findDataElements(query, options, xPath, this.indexModeOn).stream().map(WCSFemmeMapper::dataElementToCoverage).collect(Collectors.toList());
	}
	
	
	@Override
	public List<String> getCoverageIds() throws FemmeException, FemmeClientException {
		QueryOptionsMessenger options = QueryOptionsMessenger.builder().include(Sets.newHashSet("id")).build();
		return this.femmeClient.findDataElements(null, options, null)
				.stream().map(Element::getId).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getCoverageIdsInServer(List<String> filterValues) throws FemmeException, FemmeClientException {
		return getCoverageIdsInServer(filterValues, null, null, null);
	}
	
	@Override
	public List<String> getCoverageIdsInServer(List<String> filterValues, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		if (filterValues != null) {
			List<String> coverageIds = getCoverageIdsInServerByFilter("name", filterValues, limit, offset, xPath);
			
			if (coverageIds.size() == 0) {
				coverageIds = getCoverageIdsInServerByFilter("endpoint", filterValues, limit, offset, xPath);
			}
			return coverageIds;
		} else {
			return null;
		}
	}
	
	private List<String> getCoverageIdsInServerByFilter(String filterKey, List<String> filterValues, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		List<CriterionClient> serverFilterCriteria = filterValues.stream()
				.map(collectionFilterValue -> CriterionBuilderClient.root().eq(filterKey, collectionFilterValue).end())
				.collect(Collectors.toList());
		
		QueryOptionsMessenger options = QueryOptionsMessenger.builder().limit(limit).offset(offset).include(Sets.newHashSet("name")).build();
		
		return this.femmeClient.findDataElements(
				QueryClient.query().addCriterion(CriterionBuilderClient.root().inAnyCollection(serverFilterCriteria).end()),
				options, xPath)
				.stream().map(Element::getName).collect(Collectors.toList());
	}
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeException, FemmeClientException {
		return this.femmeClient.getDataElementsByName(coverageId)
				.stream().map(WCSFemmeMapper::dataElementToCoverage).collect(Collectors.toList());
	}
	
	@Override
	public List<Coverage> getCoveragesInServer(List<String> filterValue) throws FemmeException, FemmeClientException {
		return getCoveragesInServer(filterValue, null, null, null);
	}
	
	@Override
	public List<Coverage> getCoveragesInServer(List<String> filterValue, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		
		if (filterValue != null) {
			List<Coverage> coverages = getCoveragesInServerByFilter("name", filterValue, limit, offset, xPath);
			
			if (coverages.size() == 0) {
				coverages = getCoveragesInServerByFilter("endpoint", filterValue, limit, offset, xPath);
			}
			return coverages;
		} else {
			return null;
		}
		
	}
	
	private List<Coverage> getCoveragesInServerByFilter(String filterKey, List<String> filterValues, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		List<CriterionClient> serverFilterCriteria = filterValues.stream()
				.map(collectionFilterValue -> CriterionBuilderClient.root().eq(filterKey, collectionFilterValue).end())
				.collect(Collectors.toList());
		
		QueryOptionsMessenger options = QueryOptionsMessenger.builder().limit(limit).offset(offset).build();
		
		return this.femmeClient.findDataElements(
				QueryClient.query().addCriterion(CriterionBuilderClient.root().inAnyCollection(serverFilterCriteria).end()),
				options, xPath)
				.stream().map(WCSFemmeMapper::dataElementToCoverage).collect(Collectors.toList());
	}
	
	@Override
	public Coverage getCoverageByCoverageIdInServer(String key, String coverageId) throws FemmeException, FemmeClientException {
		CriterionClient collectionNameCriterion = CriterionBuilderClient.root().inAnyCollection(Collections.singletonList(CriterionBuilderClient.root().eq("name", key).end())).end();
		CriterionClient dataElementCriterion = CriterionBuilderClient.root().eq("name", coverageId).end();
		
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().and(Arrays.asList(collectionNameCriterion, dataElementCriterion)).end());
		List<Coverage> coverages = findCoverages(query, QueryOptionsMessenger.builder().limit(1).build(), null);
		
		if (coverages.size() == 0) {
			CriterionClient collectionEndpointCriterion = CriterionBuilderClient.root().inAnyCollection(
					Collections.singletonList(CriterionBuilderClient.root().eq("endpoint", key).end())).end();
			
			query = QueryClient.query().addCriterion(CriterionBuilderClient.root().and(Arrays.asList(collectionEndpointCriterion, dataElementCriterion)).end());
			coverages = findCoverages(query, QueryOptionsMessenger.builder().limit(1).build(), null);
		}
		
		return coverages.size() > 0 ? coverages.get(0) : null;
	}
	
	/*@Override
	public Coverage getCoverageByCoverageIdInServerByEndpoint(String serverEndpoint, String coverageId) throws FemmeException, FemmeClientException {
		
		CriterionClient collectionCriterion = CriterionBuilderClient.root().inAnyCollection(Arrays.asList(
				CriterionBuilderClient.root().eq("endpoint", serverEndpoint).end())).end();
		CriterionClient dataElementCriterion = CriterionBuilderClient.root().eq("name", coverageId).end();
		Query<? extends Criterion> query = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
		
		
		List<Coverage> coverages = findCoverages(query, 1, null, null, null, null, null, null);
		return coverages.size() > 0 ? coverages.get(0) : null;
		
	}

	@Override
	public Coverage getCoverageByCoverageIdInServerByAlias(String serverAlias, String coverageId) throws FemmeException, FemmeClientException {
		
		CriterionClient collectionCriterion = CriterionBuilderClient.root().inAnyCollection(Arrays.asList(
				CriterionBuilderClient.root().eq("name", serverAlias).end())).end();
		CriterionClient dataElementCriterion = CriterionBuilderClient.root().eq("name", coverageId).end();
		Query<? extends Criterion> query = QueryClient.query().addCriterion(
				CriterionBuilderClient.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
		
		
		List<Coverage> coverages = findCoverages(query, 1, null, null, null, null, null, null);
		return coverages.size() > 0 ? coverages.get(0) : null;
		
	}*/
	
	
}
