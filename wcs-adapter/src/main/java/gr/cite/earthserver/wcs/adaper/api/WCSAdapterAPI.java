package gr.cite.earthserver.wcs.adaper.api;

import java.util.List;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeDatastoreException;

public interface WCSAdapterAPI {
	
	public String insertServer(String endpoint, WCSResponse server) throws ParseException, FemmeDatastoreException ;

	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeDatastoreException ;
	
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeDatastoreException ;
	
	public String getCoverageId(List<String> endpoint) throws FemmeDatastoreException;
	
	public String getCoverageId(List<String> endpoint, String xPath) throws FemmeDatastoreException;
	
	public Server getServer(String id) throws FemmeDatastoreException;
	
	public List<Server> getServers() throws FemmeDatastoreException;
	
	public List<Server> getServers(Integer limit, Integer offset)  throws FemmeDatastoreException;
	
	public Coverage getCoverage(String id) throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages() throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages(String endpoint) throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException;
	
}
