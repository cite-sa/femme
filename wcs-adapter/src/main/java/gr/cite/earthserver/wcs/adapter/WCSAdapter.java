package gr.cite.earthserver.wcs.adapter;

import java.util.List;
import java.util.stream.Collectors;

import gr.cite.earthserver.wcs.adaper.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;

public class WCSAdapter implements WCSAdapterAPI {
	
	private FemmeClientAPI femmeClient;
	
	public WCSAdapter() {
		this.femmeClient = new FemmeClient();
	}
	
	public WCSAdapter(String femmeUrl) {
		this.femmeClient = new FemmeClient(femmeUrl);
	}
	
	@Override
	public String insertServer(String endpoint, WCSResponse server) throws ParseException, FemmeDatastoreException {
		return femmeClient.insert(WCSFemmeMapper.fromServer(endpoint, server));
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
	public List<String> getCoverageIds(List<String> endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCoverageIds(List<String> endpoint, String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Server getServer(String endpoint) throws FemmeDatastoreException {
		return WCSFemmeMapper.collectionToServer(femmeClient.getCollectionByEndpoint(endpoint));
	}
	
	@Override
	public List<Server> getServers() throws FemmeDatastoreException {
		return getServers(null, null);
	}
	
	@Override
	public List<Server> getServers(Integer limit, Integer offset) throws FemmeDatastoreException {
		return null;/*femmeClient.getCollections(limit, offset).stream().map(collection -> WCSFemmeMapper.collectionToServer(collection))
			.collect(Collectors.toList());*/
	}

	public Coverage getCoverageById(String id) throws FemmeDatastoreException {
		return WCSFemmeMapper.dataElementToCoverage(femmeClient.getDataElementById(id));
	}
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeDatastoreException {
		return null;/*femmeClient.getDataElementsByName(coverageId).stream()
				.map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
				.collect(Collectors.toList());*/
	}

	@Override
	public List<Coverage> getCoverages() throws FemmeDatastoreException {
		return getCoverages(null, null);
	}
	
	@Override
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeDatastoreException {
		return null;/*femmeClient.getDataElements(limit, offset).stream().map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
			.collect(Collectors.toList());*/
	}
	
	@Override
	public List<Coverage> getCoveragesInServer(String endpoint) throws FemmeDatastoreException {
		return getCoveragesInServer(endpoint, null, null);
	}
	
	@Override
	public List<Coverage> getCoveragesInServer(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException {
		return null;/*femmeClient.getDataElementsInCollection(endpoint, limit, offset).stream().map(dataElement -> WCSFemmeMapper.dataElementToCoverage(dataElement))
			.collect(Collectors.toList());*/
	}

	@Override
	public List<String> getCoverageIds() throws FemmeDatastoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coverage getCoverageInServerByCoverageId(String endpoint, String coverageId) throws FemmeDatastoreException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
