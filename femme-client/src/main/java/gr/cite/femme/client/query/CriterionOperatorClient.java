package gr.cite.femme.client.query;

import java.util.List;

import gr.cite.femme.query.api.ComparisonOperator;
import gr.cite.femme.query.api.CriterionInterface;
import gr.cite.femme.query.api.LogicalOperator;

public class CriterionOperatorClient extends LogicalOperatorClient {
	
	private CriterionInterface criterion;
	
	private List<CriterionInterface> criteria;
	
	private List<ComparisonOperator> comparisonOperators;
	
	private List<LogicalOperator> logicalOperators;
	
	
	public CriterionOperatorClient(CriterionInterface criterion) {
		this.criterion = criterion;
	}


	

}
