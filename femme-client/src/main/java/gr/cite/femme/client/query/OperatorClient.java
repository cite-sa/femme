package gr.cite.femme.client.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.core.query.api.Operator;

@JsonInclude(Include.NON_EMPTY)
public class OperatorClient implements Operator<CriterionClient> {
	
	private CriterionClient criterion;

	@JsonProperty/*("comparisonOperators")*/
	private List<ComparisonOperatorClient> comparisonOperators;
	
	@JsonProperty/*("logicalOperators")*/
	private List<LogicalOperatorClient> logicalOperators;
	
	@JsonProperty/*("inclusionOperators")*/
	private List<InclusionOperatorClient> inclusionOperators;
	
	
	public OperatorClient() {
			
	}
	
	public OperatorClient(CriterionClient criterion) {
		this.criterion = criterion;
		this.comparisonOperators = new ArrayList<>();
		this.logicalOperators = new ArrayList<>();
		this.inclusionOperators = new ArrayList<>();
	}

	@Override
	public Operator<CriterionClient> or(CriterionClient... criteria) {
		return or(Arrays.asList(criteria));
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
	public Operator<CriterionClient> and(CriterionClient... criteria) {
		return and(Arrays.asList(criteria));
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
	public Operator<CriterionClient> not(CriterionClient... criteria) {
		return not(Arrays.asList(criteria));
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
	public Operator<CriterionClient> nor(CriterionClient... criteria) {
		return nor(Arrays.asList(criteria));
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

	@Override
	public Operator<CriterionClient> inCollections(CriterionClient... criteria) {
		return inCollections(Arrays.asList(criteria));
	}

	public CriterionClient end() {
		return this.criterion;
	}

	@Override
	public OperatorClient inCollections(List<CriterionClient> criteria) {
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.inCollections(criteria);
		inclusionOperators.add(inclusionOperator);
		
		return this;
	}

	@Override
	public Operator<CriterionClient> inAnyCollection(CriterionClient... criteria) {
		return inAnyCollection(Arrays.asList(criteria));
	}

	@Override
	public OperatorClient inAnyCollection(List<CriterionClient> criteria) {
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.inAnyCollection(criteria);
		inclusionOperators.add(inclusionOperator);
		
		return this;
	}

	@Override
	public Operator<CriterionClient> hasDataElements(CriterionClient... criteria) {
		return hasDataElements(Arrays.asList(criteria));
	}

	@Override
	public OperatorClient hasDataElements(List<CriterionClient> criteria) {
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.hasDataElements(criteria);
		inclusionOperators.add(inclusionOperator);

		return this;
	}

	@Override
	public Operator<CriterionClient> hasAnyDataElement(CriterionClient... criteria) {
		return hasAnyDataElement(Arrays.asList(criteria));
	}

	@Override
	public OperatorClient hasAnyDataElement(List<CriterionClient> criteria) {
		InclusionOperatorClient inclusionOperator = new InclusionOperatorClient();
		inclusionOperator.hasAnyDataElement(criteria);
		inclusionOperators.add(inclusionOperator);
		
		return this;
	}

}
