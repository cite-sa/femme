package gr.cite.femme.client.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.Operator;

@JsonInclude(Include.NON_EMPTY)
public class OperatorClient implements Operator<CriterionClient> {
	
	private CriterionClient criterion;

	@JsonProperty
	private List<ComparisonOperatorClient> comparisonOperators;
	
	@JsonProperty
	private List<LogicalOperatorClient> logicalOperators;
	
	@JsonProperty
	private List<InclusionOperatorClient> inclusionOperators;
	
	
	public OperatorClient() {
			
	}
	
	public OperatorClient(CriterionClient criterion) {
		this.criterion = criterion;
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
		inclusionOperators = new ArrayList<>();
	}

	@Override
	public OperatorClient or(List<CriterionClient> criteria) {
		/*initializeLogicalOperatorsList();*/
		
		LogicalOperatorClient logicalOperator = new LogicalOperatorClient();
		logicalOperator.or(criteria);
		
		logicalOperators.add(logicalOperator);
		
		return this;
		
	}

	@Override
	public OperatorClient and(List<CriterionClient> criteria) {
		/*initializeLogicalOperatorsList();*/
		
		LogicalOperatorClient logicalOperator = new LogicalOperatorClient();
		logicalOperator.and(criteria);
		
		logicalOperators.add(logicalOperator);
		
		return this;
	}

	@Override
	public OperatorClient not(List<CriterionClient> criteria) {
		/*initializeLogicalOperatorsList();*/
		
		LogicalOperatorClient logicalOperator = new LogicalOperatorClient();
		logicalOperator.and(criteria);
		
		logicalOperators.add(logicalOperator);
		
		return this;
	}

	@Override
	public OperatorClient nor(List<CriterionClient> criteria) {
		/*initializeLogicalOperatorsList();*/
		
		LogicalOperatorClient logicalOperator = new LogicalOperatorClient();
		logicalOperator.nor(criteria);
		
		logicalOperators.add(logicalOperator);
		
		return this;
	}

	@Override
	public OperatorClient eq(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.eq(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient gt(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.gt(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient gte(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.gte(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient lt(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.lt(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient lte(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.lte(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient ne(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.ne(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient in(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.in(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	@Override
	public OperatorClient nin(String field, Object value) {
		/*initializeComparisonOperatorsList();*/
		
		ComparisonOperatorClient comparisonOperator = new ComparisonOperatorClient();
		comparisonOperator.nin(field, value);
		
		comparisonOperators.add(comparisonOperator);
		
		return this;
	}

	public CriterionClient end() {
		return criterion;
	}

	@Override
	public Operator<CriterionClient> inCollections(List<CriterionClient> criteria) {
		
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.inCollections(criteria);
		
		inclusionOperators.add(inclusionOperator);
		
		return this;
		
	}

	@Override
	public Operator<CriterionClient> inAnyCollection(List<CriterionClient> criteria) {
		
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.inAnyCollection(criteria);
		
		inclusionOperators.add(inclusionOperator);
		
		return this;
		
	}

	@Override
	public Operator<CriterionClient> hasDataElements(List<CriterionClient> criteria) {
		
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.hasDataElements(criteria);
		
		inclusionOperators.add(inclusionOperator);
		
		return this;
		
	}

	@Override
	public Operator<CriterionClient> hasAnyDataElement(List<CriterionClient> criteria) {
		
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.hasAnyDataElement(criteria);
		
		inclusionOperators.add(inclusionOperator);
		
		return this;
		
	}

}
