package gr.cite.earthserver.wcs.adapter;

import gr.cite.earthserver.wcs.adaper.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.api.FemmeClientAPI;

public class WCSAdapter implements WCSAdapterAPI {
	
	private FemmeClientAPI femmeClient;
	
	public WCSAdapter() {
		this.femmeClient = new FemmeClient();
	}
	
	@Override
	public String insertServer(String endpoint, WCSResponse server) throws ParseException {
		return femmeClient.insert(WCSFemmeMapper.fromServer(endpoint, server));
	}

	@Override
	public String insertCoverage(WCSResponse coverage) throws ParseException {
		return femmeClient.insert(WCSFemmeMapper.fromCoverage(coverage));
	}
	
	@Override
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException {
		return femmeClient.addToCollection(WCSFemmeMapper.fromCoverage(coverage), collectionId);
	}
}
