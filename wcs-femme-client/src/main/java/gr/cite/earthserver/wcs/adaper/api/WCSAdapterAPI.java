package gr.cite.earthserver.wcs.adaper.api;

import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;

public interface WCSAdapterAPI {
	
	public String insertServer(String endpoint, WCSResponse server) throws ParseException;

	public String insertCoverage(WCSResponse coverage) throws ParseException;
	
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException;
	
}
