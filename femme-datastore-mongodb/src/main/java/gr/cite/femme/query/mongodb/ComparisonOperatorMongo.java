package gr.cite.femme.query.mongodb;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.client.model.Filters;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.ComparisonOperator;
import gr.cite.femme.utils.Pair;

@JsonInclude(Include.NON_EMPTY)
public class ComparisonOperatorMongo implements ComparisonOperator {
	
	private static final Logger logger = LoggerFactory.getLogger(ComparisonOperatorMongo.class); 
	
	protected static enum ComparisonOperator {
		EQ("$eq"),
		GT("$gt"),
		GTE("$gte"),
		LT("$lt"),
		LTE("$lte"),
		NE("$ne"),
		IN("$in"),
		NIN("$nin");
		
		private String comparisonOperatorCode;
		
		private ComparisonOperator(String comparisonOperatorCode) {
			this.comparisonOperatorCode = comparisonOperatorCode;
		}
		
		protected String getComparisonOperatorCode() {
			return comparisonOperatorCode;
		}
		
	}
	@JsonProperty
	private String field;
	
	@JsonProperty
	private ComparisonOperator operator;
	
	@JsonProperty
	private Object value;
	
	@Override
	public void eq(String field, Object value) {
		setFields(field, ComparisonOperator.EQ, value);
	}

	@Override
	public void gt(String field, Object value) {
		setFields(field, ComparisonOperator.GT, value);
		
	}

	@Override
	public void gte(String field, Object value) {
		setFields(field, ComparisonOperator.GTE, value);
		
	}

	@Override
	public void lt(String field, Object value) {
		setFields(field, ComparisonOperator.LT, value);
		
	}

	@Override
	public void lte(String field, Object value) {
		setFields(field, ComparisonOperator.LT, value);
		
	}

	@Override
	public void ne(String field, Object value) {
		setFields(field, ComparisonOperator.NE, value);
		
	}

	@Override
	public void in(String field, Object value) {
		setFields(field, ComparisonOperator.IN, value);
		
	}

	@Override
	public void nin(String field, Object value) {
		setFields(field, ComparisonOperator.NIN, value);
		
	}
	
	private void setFields(String field, ComparisonOperator operator, Object value) {
		this.field = field;
		this.operator = operator;
		this.value = value;
	}
	
	protected String getField() {
		return field;
	}

	protected ComparisonOperator getOperator() {
		return operator;
	}

	protected Object getValue() {
		return value;
	}
	
	protected Pair<String, Document> build() {
		Document comparisonDoc = new Document().append(operator.getComparisonOperatorCode(), value);
		Pair<String, Document> pair = new Pair<>(field, comparisonDoc);
		
		return pair;
		/*Document doc = new Document().append(field, comparisonDoc);*/
		/*String string = "" + field + ":{" + operator.getComparisonOperatorCode() + ":" + "\"" + value + "\"" + "}";*/
		/*return doc;*/
	}
	
}