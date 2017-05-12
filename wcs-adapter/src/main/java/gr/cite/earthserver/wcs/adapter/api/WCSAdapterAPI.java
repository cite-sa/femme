package gr.cite.earthserver.wcs.adapter.api;

import java.util.List;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;

public interface WCSAdapterAPI {

	public String beginImport(String endpoint) throws FemmeException;

	public void endImport(String importId) throws FemmeException;

	public String importServer(String importId, String endpoint, String name, WCSResponse server) throws ParseException, FemmeException;

	public String importCoverage(String importId, WCSResponse coverage) throws ParseException, FemmeException;

	
	public String insertServer(String endpoint, String name, WCSResponse server) throws ParseException, FemmeException;

	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeException;
	
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeException;
	
	
	public List<Server> getServers() throws FemmeException, FemmeClientException;
	
	public List<Server> getServers(Integer limit, Integer offset) throws FemmeException, FemmeClientException;
	
	public List<Server> getServers(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;
	
	public Server getServer(String filterValue) throws FemmeException, FemmeClientException;
	
	/*public Server getServerByAlias(String alias) throws FemmeException, FemmeClientException;*/
	
	public <T extends Criterion> List<Server> findServers(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException;
	
	
	public List<Coverage> getCoverages() throws FemmeException, FemmeClientException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset) throws FemmeException, FemmeClientException;
	
	public List<Coverage> getCoverages(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;
	
	public Coverage getCoverageById(String id) throws FemmeException, FemmeClientException;
	
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException;
	
	
	public List<String> getCoverageIds() throws FemmeException, FemmeClientException;
	
	public List<String> getCoverageIdsInServer(List<String> filterValues) throws FemmeException, FemmeClientException;
	
	public List<String> getCoverageIdsInServer(List<String> filterValues, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;
	
	/*public List<String> getCoverageIdsByServerAlias(List<String> serverAliases) throws FemmeException, FemmeClientException;*/
	
	
	public List<Coverage> getCoveragesByCoverageId(String coverageId) throws FemmeException, FemmeClientException;

	
	public List<Coverage> getCoveragesInServer(List<String> filterValue) throws FemmeException, FemmeClientException;
	
	public List<Coverage> getCoveragesInServer(List<String> filterValue, Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;
	
	public Coverage getCoverageByCoverageIdInServer(String key, String coverageId) throws FemmeException, FemmeClientException;
		
	/*public Coverage getCoverageByCoverageIdInServerByEndpoint(String serverEndpoint, String coverageId) throws FemmeException, FemmeClientException;
	
	public Coverage getCoverageByCoverageIdInServerByAlias(String serverAlias, String coverageId) throws FemmeException, FemmeClientException;*/
	
	
	
	
}
