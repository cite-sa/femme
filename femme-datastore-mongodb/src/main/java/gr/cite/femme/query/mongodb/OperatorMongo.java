package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.query.api.Operator;
import gr.cite.femme.query.mongodb.ComparisonOperatorMongo.ComparisonOperator;
import gr.cite.femme.query.mongodb.InclusionOperatorMongo.InclusionOperator;
import gr.cite.femme.query.mongodb.LogicalOperatorMongo.LogicalOperator;

@JsonInclude(Include.NON_EMPTY)
public class OperatorMongo implements Operator<CriterionMongo> {
	
	private CriterionMongo criterion;
	
	@JsonProperty
	private List<ComparisonOperatorMongo> comparisonOperators;
	
	@JsonProperty
	private List<LogicalOperatorMongo> logicalOperators;
	
	@JsonProperty
	private List<InclusionOperatorMongo> inclusionOperators;
	
	/*@JsonSetter
	public void setCriterion(CriterionMongo criterion) {
		this.criterion = criterion;
	}*/
	
	public OperatorMongo() {
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
		inclusionOperators = new ArrayList<>();
	}
	
	public OperatorMongo(CriterionMongo criterion) {
		this.criterion = criterion;
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
		inclusionOperators = new ArrayList<>();
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
	
	@Override
	public OperatorMongo inCollections(List<CriterionMongo> collectionCriteria) {
		addInclusionOperator(InclusionOperator.IN_COLLECTIONS, collectionCriteria);
		return this;
	}
	
	@Override
	public OperatorMongo inAnyCollection(List<CriterionMongo> collectionCriteria) {
		addInclusionOperator(InclusionOperator.IN_ANY_COLLECTION, collectionCriteria);
		return this;
	}

	@Override
	public OperatorMongo hasDataElements(List<CriterionMongo> dataElementCriteria) {
		addInclusionOperator(InclusionOperator.HAS_DATA_ELEMENTS, dataElementCriteria);
		return this;
	}
	
	

	@Override
	public OperatorMongo hasAnyDataElement(List<CriterionMongo> dataElementCriteria) {
		addInclusionOperator(InclusionOperator.HAS_ANY_DATA_ELEMENT, dataElementCriteria);
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
		/*if (FieldNames.ID.equals(field)) {
			value = new ObjectId(value.toString());
		}*/
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
	
	private void addInclusionOperator(InclusionOperator inclusionOperatorType, List<CriterionMongo> criteria) {
		InclusionOperatorMongo inclusionOperator = new InclusionOperatorMongo();
		switch (inclusionOperatorType) {
		case IN_COLLECTIONS:
			inclusionOperator.inCollections(criteria);
			break;
		case IN_ANY_COLLECTION:
			inclusionOperator.inAnyCollection(criteria);
			break;
		case HAS_DATA_ELEMENTS:
			inclusionOperator.hasDataElements(criteria);
			break;
		case HAS_ANY_DATA_ELEMENT:
			inclusionOperator.hasAnyDataElement(criteria);
			break;
			
		default:
			break;
		}
		
		inclusionOperators.add(inclusionOperator);
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
		 
		comparisonOperators.stream().map(comparisonOperator -> comparisonOperator.build())
			.forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		logicalOperators.stream().map(logicalOperator -> logicalOperator.build())
			.forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		inclusionOperators.stream().map(inclusionOperator -> inclusionOperator.build())
			.forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		
		return doc;
		
	}

}