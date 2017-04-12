package gr.cite.femme.client.query;

import java.util.List;

import gr.cite.femme.core.query.api.ComparisonOperator;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.LogicalOperator;

public class CriterionOperatorClient extends LogicalOperatorClient {
	
	private Criterion criterion;
	
	private List<Criterion> criteria;
	
	private List<ComparisonOperator> comparisonOperators;
	
	private List<LogicalOperator> logicalOperators;
	
	
	public CriterionOperatorClient(Criterion criterion) {
		this.criterion = criterion;
	}


	

}
