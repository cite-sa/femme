package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.LogicalOperator;
import gr.cite.femme.utils.Pair;

@JsonInclude(Include.NON_EMPTY)
public class LogicalOperatorMongo implements LogicalOperator<CriterionMongo> {
	
	protected static enum LogicalOperator {
		OR("$or"),
		AND("$and"),
		NOT("$not"),
		NOR("$nor");
		
		private String logicalOperatorCode;
		
		private LogicalOperator(String logicalOperatorCode) {
			this.logicalOperatorCode = logicalOperatorCode;
		}
		
		protected String getLogicalOperatorCode() {
			return logicalOperatorCode;
		}
	}
	
	@JsonProperty
	private LogicalOperator operator;
	
	@JsonProperty
	private List<CriterionMongo> criteria;
	

	@Override
	public void or(List<CriterionMongo> criteria) {
		setFileds(LogicalOperator.OR, criteria);
	}

	@Override
	public void and(List<CriterionMongo> criteria) {
		setFileds(LogicalOperator.AND, criteria);
	}

	@Override
	public void not(List<CriterionMongo> criteria) {
		setFileds(LogicalOperator.NOT, criteria);
	}

	@Override
	public void nor(List<CriterionMongo> criteria) {
		setFileds(LogicalOperator.NOR, criteria);
	}
	
	private void setFileds(LogicalOperator operator, List<CriterionMongo> criteria) {
		this.operator = operator;
		this.criteria = criteria;
	}

	protected LogicalOperator getOperator() {
		return operator;
	}

	protected List<CriterionMongo> getCriteria() {
		return criteria;
	}
	
	protected Pair<String, List<Document>> build() {
		
		List<Document> criteriaDocuments = new ArrayList<>();
		if (criteria != null) {
			for (CriterionMongo criterion : criteria) {
				criteriaDocuments.add(criterion.build());
			}
		}
		
		Pair<String, List<Document>> pair = new Pair<>(operator.getLogicalOperatorCode(), criteriaDocuments);
		return pair;
		
	}
	
}
