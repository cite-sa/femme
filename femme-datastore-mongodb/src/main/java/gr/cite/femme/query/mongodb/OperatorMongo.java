package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.Operator;
import gr.cite.femme.query.mongodb.ComparisonOperatorMongo.ComparisonOperator;
import gr.cite.femme.query.mongodb.LogicalOperatorMongo.LogicalOperator;
import gr.cite.femme.utils.Pair;

@JsonInclude(Include.NON_EMPTY)
public class OperatorMongo implements Operator<CriterionMongo> {
	
	private static final Logger logger = LoggerFactory.getLogger(OperatorMongo.class);
	
	private CriterionMongo criterion;
	
	@JsonProperty
	private List<ComparisonOperatorMongo> comparisonOperators;
	
	@JsonProperty
	private List<LogicalOperatorMongo> logicalOperators;
	
	/*@JsonSetter
	public void setCriterion(CriterionMongo criterion) {
		this.criterion = criterion;
	}*/
	
	public OperatorMongo() {
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
	}
	
	public OperatorMongo(CriterionMongo criterion) {
		this.criterion = criterion;
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
	}

	@Override
	public OperatorMongo or(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperator.OR, criteria);
		return this;
	}

	@Override
	public OperatorMongo and(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperator.AND, criteria);
		return this;
	}

	@Override
	public OperatorMongo not(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperator.NOT, criteria);
		return this;
	}

	@Override
	public OperatorMongo nor(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperator.NOR, criteria);
		return this;
	}

	@Override
	public OperatorMongo eq(String field, Object value) {
		addComparisonOperator(ComparisonOperator.EQ, field, value);
		return this;
	}

	@Override
	public OperatorMongo gt(String field, Object value) {
		addComparisonOperator(ComparisonOperator.GT, field, value);
		return this;
	}

	@Override
	public OperatorMongo gte(String field, Object value) {
		addComparisonOperator(ComparisonOperator.GTE, field, value);
		return this;
	}

	@Override
	public OperatorMongo lt(String field, Object value) {
		addComparisonOperator(ComparisonOperator.LT, field, value);
		return this;
	}

	@Override
	public OperatorMongo lte(String field, Object value) {
		addComparisonOperator(ComparisonOperator.LTE, field, value);
		return this;
	}

	@Override
	public OperatorMongo ne(String field, Object value) {
		addComparisonOperator(ComparisonOperator.NE, field, value);
		return this;
	}

	@Override
	public OperatorMongo in(String field, Object value) {
		addComparisonOperator(ComparisonOperator.IN, field, value);
		return this;
	}

	@Override
	public OperatorMongo nin(String field, Object value) {
		addComparisonOperator(ComparisonOperator.NIN, field, value);
		return this;
	}
	
	private void addLogicalOperator(LogicalOperator logicalOperatorType, List<CriterionMongo> criteria) {
		LogicalOperatorMongo logicalOperator = new LogicalOperatorMongo();
		switch (logicalOperatorType) {
		case OR:
			logicalOperator.or(criteria);
			break;
		case AND:
			logicalOperator.and(criteria);
			break;
		case NOR:
			logicalOperator.nor(criteria);
			break;
		case NOT:
			logicalOperator.not(criteria);
			break;

		default:
			break;
		}
		
		logicalOperators.add(logicalOperator);
	}
	
	private void addComparisonOperator(ComparisonOperator comparisonOperatorType, String field, Object value) {
		ComparisonOperatorMongo comparisonOperator = new ComparisonOperatorMongo();
		switch (comparisonOperatorType) {
		case EQ:
			comparisonOperator.eq(field, value);
			break;
		case GT:
			comparisonOperator.gt(field, value);
			break;
		case GTE:
			comparisonOperator.gte(field, value);
			break;
		case LT:
			comparisonOperator.lt(field, value);
			break;
		case LTE:
			comparisonOperator.lte(field, value);
			break;
		case NE:
			comparisonOperator.ne(field, value);
			break;
		case IN:
			comparisonOperator.in(field, value);
			break;
		case NIN:
			comparisonOperator.nin(field, value);
			break;
			
		default:
			break;
		}
		
		
		comparisonOperators.add(comparisonOperator);
	}
	
	public CriterionMongo end() {
		return criterion;
	}
	
	protected List<ComparisonOperatorMongo> getComparisonOperators() {
		return comparisonOperators;
	}
	
	protected List<LogicalOperatorMongo> getLogicalOperators() {
		return logicalOperators;
	}
	
	protected Document build() {
		Document doc = new Document();
		//List<Pair<>> operators = new ArrayList<>();
		 
		comparisonOperators.stream().map(comparisonOperator -> comparisonOperator.build())
			.forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		logicalOperators.stream().map(logicalOperator -> logicalOperator.build())
			.forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		
		/*doc.append("$and", operators);*/
		return doc;
		/*for (ComparisonOperatorMongo comparisonOperator: comparisonOperators) {
			doc.append(comparisonOperator.getField(),
					new Document().append(comparisonOperator.getOperator().getComparisonOperatorCode(), comparisonOperator.getValue()));
		}*/
		
		/*String string = "";
		
		List<String> operators = new ArrayList<>();
		comparisonOperators.stream().map(comparisonOperator -> comparisonOperator.build()).forEach(operators::add);
		logicalOperators.stream().map(logicalOperator -> logicalOperator.build()).forEach(operators::add);
		
		Iterator<String> operatorsIterator = operators.iterator();
		while (operatorsIterator.hasNext()) {
			string += operatorsIterator.next();
			if (operatorsIterator.hasNext()) {
				string += ",";
			}
			
		}*/

		/*return operators;*/
	}
	
}