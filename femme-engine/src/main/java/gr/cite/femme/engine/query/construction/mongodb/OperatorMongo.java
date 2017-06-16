package gr.cite.femme.engine.query.construction.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gr.cite.femme.core.query.construction.Operator;
import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class OperatorMongo implements Operator<CriterionMongo> {
	
	private CriterionMongo criterion;
	
	@JsonProperty/*("comparisonOperators")*/
	private List<ComparisonOperatorMongo> comparisonOperators;
	
	@JsonProperty/*("logicalOperators")*/
	private List<LogicalOperatorMongo> logicalOperators;
	
	@JsonProperty/*("inclusionOperators")*/
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
	
	OperatorMongo(CriterionMongo criterion) {
		this.criterion = criterion;
		comparisonOperators = new ArrayList<>();
		logicalOperators = new ArrayList<>();
		inclusionOperators = new ArrayList<>();
	}

	@Override
	public Operator<CriterionMongo> or(CriterionMongo... criteria) {
		return or(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo or(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperatorMongo.LogicalOperator.OR, criteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> and(CriterionMongo... criteria) {
		return and(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo and(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperatorMongo.LogicalOperator.AND, criteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> not(CriterionMongo... criteria) {
		return not(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo not(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperatorMongo.LogicalOperator.NOT, criteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> nor(CriterionMongo... criteria) {
		return nor(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo nor(List<CriterionMongo> criteria) {
		addLogicalOperator(LogicalOperatorMongo.LogicalOperator.NOR, criteria);
		return this;
	}

	@Override
	public OperatorMongo eq(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.EQ, field, value);
		return this;
	}

	@Override
	public OperatorMongo gt(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.GT, field, value);
		return this;
	}

	@Override
	public OperatorMongo gte(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.GTE, field, value);
		return this;
	}

	@Override
	public OperatorMongo lt(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.LT, field, value);
		return this;
	}

	@Override
	public OperatorMongo lte(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.LTE, field, value);
		return this;
	}

	@Override
	public OperatorMongo ne(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.NE, field, value);
		return this;
	}

	@Override
	public OperatorMongo in(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.IN, field, value);
		return this;
	}

	@Override
	public OperatorMongo nin(String field, Object value) {
		addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator.NIN, field, value);
		return this;
	}

	@Override
	public Operator<CriterionMongo> inCollections(CriterionMongo... criteria) {
		return inCollections(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo inCollections(List<CriterionMongo> collectionCriteria) {
		addInclusionOperator(InclusionOperatorMongo.InclusionOperator.IN_COLLECTIONS, collectionCriteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> inAnyCollection(CriterionMongo... criteria) {
		return inAnyCollection(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo inAnyCollection(List<CriterionMongo> collectionCriteria) {
		addInclusionOperator(InclusionOperatorMongo.InclusionOperator.IN_ANY_COLLECTION, collectionCriteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> hasDataElements(CriterionMongo... criteria) {
		return hasDataElements(Arrays.asList(criteria));
	}

	@Override
	public OperatorMongo hasDataElements(List<CriterionMongo> dataElementCriteria) {
		addInclusionOperator(InclusionOperatorMongo.InclusionOperator.HAS_DATA_ELEMENTS, dataElementCriteria);
		return this;
	}

	@Override
	public Operator<CriterionMongo> hasAnyDataElement(CriterionMongo... criteria) {
		return hasAnyDataElement(Arrays.asList(criteria));
	}


	@Override
	public OperatorMongo hasAnyDataElement(List<CriterionMongo> dataElementCriteria) {
		addInclusionOperator(InclusionOperatorMongo.InclusionOperator.HAS_ANY_DATA_ELEMENT, dataElementCriteria);
		return this;
	}
	
	private void addLogicalOperator(LogicalOperatorMongo.LogicalOperator logicalOperatorType, List<CriterionMongo> criteria) {
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
	
	private void addComparisonOperator(ComparisonOperatorMongo.ComparisonOperator comparisonOperatorType, String field, Object value) {
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
	
	private void addInclusionOperator(InclusionOperatorMongo.InclusionOperator inclusionOperatorType, List<CriterionMongo> criteria) {
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
		comparisonOperators.stream().map(ComparisonOperatorMongo::build).forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		logicalOperators.stream().map(LogicalOperatorMongo::build).forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		inclusionOperators.stream().map(InclusionOperatorMongo::build).forEach(pair -> doc.append(pair.getLeft(), pair.getRight()));
		
		return doc;
	}

}