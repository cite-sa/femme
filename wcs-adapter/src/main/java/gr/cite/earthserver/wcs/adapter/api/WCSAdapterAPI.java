package gr.cite.earthserver.wcs.adapter.api;

import java.util.List;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryOptionsFields;

public interface WCSAdapterAPI {
	
	public String insertServer(String endpoint, String name, WCSResponse server) throws ParseException, FemmeDatastoreException ;

	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeDatastoreException ;
	
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeDatastoreException ;
	
	
	public List<Server> getServers() throws FemmeDatastoreException, FemmeClientException;
	
	public List<Server> getServers(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;
	
	public List<Server> getServers(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;
	
	public Server getServer(String filterValue) throws FemmeDatastoreException, FemmeClientException;
	
	/*public Server getServerByAlias(String alias) throws FemmeDatastoreException, FemmeClientException;*/
	
	public <T extends Criterion> List<Server> findServers(Query<T> query, QueryOptionsFields options, String xPath)
			throws FemmeDatastoreException, FemmeClientException;
	
	
	public List<Coverage> getCoverages() throws FemmeDatastoreException, FemmeClientException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;
	
	public Coverage getCoverageById(String id) throws FemmeDatastoreException, FemmeClientException;
	
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, QueryOptionsFields options, String xPath)
			throws FemmeDatastoreException, FemmeClientException, FemmeClientException;
	
	
	public List<String> getCoverageIds() throws FemmeDatastoreException, FemmeClientException;
	
	public List<String> getCoverageIdsInServer(List<String> filterValues) throws FemmeDatastoreException, FemmeClientException;
	
	public List<String> getCoverageIdsInServer(List<String> filterValues, Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;
	
	/*public List<String> getCoverageIdsByServerAlias(List<String> serverAliases) throws FemmeDatastoreException, FemmeClientException;*/
	
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeDatastoreException, FemmeClientException;

	
	public List<Coverage> getCoveragesInServer(List<String> filterValue) throws FemmeDatastoreException, FemmeClientException;
	
	public List<Coverage> getCoveragesInServer(List<String> filterValue, Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;
	
	public Coverage getCoverageByCoverageIdInServer(String key, String coverageId) throws FemmeDatastoreException, FemmeClientException;
		
	/*public Coverage getCoverageByCoverageIdInServerByEndpoint(String serverEndpoint, String coverageId) throws FemmeDatastoreException, FemmeClientException;
	
	public Coverage getCoverageByCoverageIdInServerByAlias(String serverAlias, String coverageId) throws FemmeDatastoreException, FemmeClientException;*/
	
	
	
	
}
