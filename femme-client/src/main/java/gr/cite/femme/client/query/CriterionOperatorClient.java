package gr.cite.femme.client.query;

import java.util.List;

import gr.cite.femme.core.query.construction.ComparisonOperator;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.LogicalOperator;

public class CriterionOperatorClient extends LogicalOperatorClient {
	
	private Criterion criterion;
	
	private List<Criterion> criteria;
	
	private List<ComparisonOperator> comparisonOperators;
	
	private List<LogicalOperator> logicalOperators;
	
	
	public CriterionOperatorClient(Criterion criterion) {
		this.criterion = criterion;
	}


	

}
