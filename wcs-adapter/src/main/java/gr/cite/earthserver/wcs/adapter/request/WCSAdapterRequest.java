package gr.cite.earthserver.wcs.adapter.request;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public static WCSAdapterRequest request() {
		return new WCSAdapterRequest();
	}
	
	public WCSAdapterServers servers() {
		servers = new WCSAdapterServers(this);
		return servers;
	}
	
	protected void execute() {
		
	}

	protected WCSAdapterServers getServers() {
		return servers;
	}

	protected void setServers(WCSAdapterServers servers) {
		this.servers = servers;
	}

	protected WCSAdapterCoverages getCoverages() {
		return coverages;
	}

	protected void setCoverages(WCSAdapterCoverages coverages) {
		this.coverages = coverages;
	}
	
	protected QueryClient mapToQuery() {
		CriterionClient coverageCriterion;
		OperatorClient coverageOperator = CriterionBuilderClient.root();
		
		List<CriterionClient> coverageCriteria = coverages != null ?
			coverages.getCoveragesProperties().entries().stream().map(property -> {
				return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
			}).collect(Collectors.toList()) :
				new ArrayList<>();
		
		if (coverages.isAnd()) {
			coverageOperator.and(coverageCriteria);
		} else if (coverages.isOr()) {
			coverageOperator.or(coverageCriteria);
		}
		
		
		List<CriterionClient> serverCriteria = servers!= null ? 
				servers.getServersProperties().entries().stream().map(property -> {
					return CriterionBuilderClient.root().eq(property.getKey(), property.getValue()).end();
				}).collect(Collectors.toList()) :
					new ArrayList<>();
		
		if (servers.isAnd()) {
			CriterionClient andServerCriterion = CriterionBuilderClient.root().and(serverCriteria).end();
			coverageOperator.inCollections(Arrays.asList(andServerCriterion));
		} else if (servers.isOr()) {
			CriterionClient orServerCriterion = CriterionBuilderClient.root().or(serverCriteria).end();
			coverageOperator.inAnyCollection(Arrays.asList(orServerCriterion));
		}
		coverageCriterion = coverageOperator.end();
		
		return new QueryClient().addCriterion(coverageCriterion);
	}
	
}
