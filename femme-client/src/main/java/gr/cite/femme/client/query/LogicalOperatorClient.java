package gr.cite.femme.client.query;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.LogicalOperator;

@JsonInclude(Include.NON_EMPTY)
public class LogicalOperatorClient implements LogicalOperator<CriterionClient> {
	
	@JsonProperty
	private LogicalOperators operator;
	
	@JsonProperty
	private List<CriterionClient> criteria;
	

	@Override
	public void or(List<CriterionClient> criteria) {
		operator = LogicalOperators.OR;
		this.criteria = criteria;
	}

	@Override
	public void and(List<CriterionClient> criteria) {
		operator = LogicalOperators.AND;
		this.criteria = criteria;
	}

	@Override
	public void not(List<CriterionClient> criteria) {
		operator = LogicalOperators.NOT;
		this.criteria = criteria;
	}

	@Override
	public void nor(List<CriterionClient> criteria) {
		operator = LogicalOperators.NOR;
		this.criteria = criteria;
	}

	
	private static enum LogicalOperators {
		OR("or"),
		AND("and"),
		NOT("not"),
		NOR("not");
		
		private String logicalOperatorCode;
		
		LogicalOperators(String logicalOperatorCode) {
			this.logicalOperatorCode = logicalOperatorCode;
		}
		
		public String getLogicalOperatorCode() {
			return logicalOperatorCode;
		}
	}
	
}
