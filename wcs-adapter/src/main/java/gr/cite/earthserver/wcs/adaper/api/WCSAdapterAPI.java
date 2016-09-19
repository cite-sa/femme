package gr.cite.earthserver.wcs.adaper.api;

import java.util.List;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;

public interface WCSAdapterAPI {
	
	public String insertServer(String endpoint, WCSResponse server) throws ParseException, FemmeDatastoreException ;

	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeDatastoreException ;
	
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeDatastoreException ;
	
	
	public List<String> getCoverageIds() throws FemmeDatastoreException;
	
	public List<String> getCoverageIds(List<String> endpoint) throws FemmeDatastoreException;
	
	public List<String> getCoverageIds(List<String> endpoint, String xPath) throws FemmeDatastoreException;
	
	
	public Server getServer(String endpoint) throws FemmeDatastoreException;
	
	public List<Server> getServers() throws FemmeDatastoreException;
	
	public List<Server> getServers(Integer limit, Integer offset)  throws FemmeDatastoreException;
	
	
	public Coverage getCoverageById(String id) throws FemmeDatastoreException;
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeDatastoreException;
	
	public Coverage getCoverageInServerByCoverageId(String endpoint, String coverageId) throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages() throws FemmeDatastoreException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public List<Coverage> getCoveragesInServer(String endpoint) throws FemmeDatastoreException;
	
	public List<Coverage> getCoveragesInServer(String endpoint, Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, Integer limit, Integer offset, String xPath) throws FemmeDatastoreException;
	
}
