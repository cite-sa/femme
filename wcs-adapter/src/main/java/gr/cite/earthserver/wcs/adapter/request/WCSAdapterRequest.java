package gr.cite.earthserver.wcs.adapter.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.query.OperatorClient;
import gr.cite.femme.client.query.QueryClient;

public class WCSAdapterRequest {
	
	private WCSAdapterServers servers;
	
	private WCSAdapterCoverages coverages;
	
	public WCSAdapterRequest() {
		servers = new WCSAdapterServers();
		coverages = new WCSAdapterCoverages();
	}
	
	public WCSAdapterRequest(WCSAdapterServers servers, WCSAdapterCoverages coverages) {
		this.servers = servers;
		this.coverages = coverages;
	}
	
	public static WCSAdapterRequest request() {
		return new WCSAdapterRequest();
	}
	
	public WCSAdapterServers servers() {
		servers = new WCSAdapterServers(this);
		return servers;
	}
	
	public WCSAdapterCoverages coverages() {
		coverages = new WCSAdapterCoverages(this);
		return coverages;
	}
	
	public WCSAdapterServers getServers() {
		return servers;
	}

	public void setServers(WCSAdapterServers servers) {
		this.servers = servers;
	}

	public WCSAdapterCoverages getCoverages() {
		return coverages;
	}

	public void setCoverages(WCSAdapterCoverages coverages) {
		this.coverages = coverages;
	}
	
	public QueryClient mapToQuery() {
		/*CriterionClient coverageCriterion;
		OperatorClient coverageOperator = CriterionBuilderClient.root();*/
		
		List<CriterionClient> andCoverageCriteria = coverages != null ?
				coverages.getAndCoverageAttributes().entries().stream().map(property -> {
					return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
				}).collect(Collectors.toList()) :
					new ArrayList<>();
				
		List<CriterionClient> orCoverageCriteria = coverages != null ?
			coverages.getOrCoverageAttributes().entries().stream().map(property -> {
				return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
			}).collect(Collectors.toList()) :
				new ArrayList<>();
		
		OperatorClient coverageOperator = CriterionBuilderClient.root();
		if (andCoverageCriteria.size() > 0) {
			coverageOperator.and(andCoverageCriteria);
		}
		if (orCoverageCriteria.size() > 0) {
			coverageOperator.or(orCoverageCriteria);
		}
	
		
		List<CriterionClient> andServerCriteria = servers!= null ? 
				servers.getAndServerAttributes().entries().stream().map(property -> {
					return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
				}).collect(Collectors.toList()) :
					new ArrayList<>();
				
		List<CriterionClient> orServerCriteria = servers!= null ? 
				servers.getOrServerAttributes().entries().stream().map(property -> {
					return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
				}).collect(Collectors.toList()) :
					new ArrayList<>();
		
		if (andServerCriteria.size() > 0) {
			coverageOperator.inCollections(andServerCriteria);
		} else if (orServerCriteria.size() > 0) {
			coverageOperator.inAnyCollection(orServerCriteria);
		}
			
		CriterionClient coverageCriterion = coverageOperator.end();
		
		return new QueryClient().addCriterion(coverageCriterion);
	}
	
}
