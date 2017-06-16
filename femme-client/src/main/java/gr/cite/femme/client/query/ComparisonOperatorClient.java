package gr.cite.femme.client.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.core.query.construction.ComparisonOperator;

@JsonInclude(Include.NON_EMPTY)
public class ComparisonOperatorClient implements ComparisonOperator {
	
	@JsonProperty
	private String field;
	
	@JsonProperty
	private ComparisonOperators operator;
	
	@JsonProperty
	private Object value;

	

	@Override
	public void eq(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.EQ;
		this.value = value;
	}

	@Override
	public void gt(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.GT;
		this.value = value;		
	}

	@Override
	public void gte(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.GTE;
		this.value = value;
	}

	@Override
	public void lt(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.LT;
		this.value = value;
	}

	@Override
	public void lte(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.LTE;
		this.value = value;
	}

	@Override
	public void ne(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.NE;
		this.value = value;
	}


	@Override
	public void in(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.IN;
		this.value = value;
	}


	@Override
	public void nin(String field, Object value) {
		this.field = field;
		operator = ComparisonOperators.NIN;
		this.value = value;
	}
	
	private static enum ComparisonOperators {
		EQ("eq"),
		GT("gt"),
		GTE("gte"),
		LT("lt"),
		LTE("lte"),
		NE("ne"),
		IN("in"),
		NIN("nin");
		
		private String comparisonOperatorCode;
		
		ComparisonOperators(String comparisonOperatorCode) {
			
		}
		
		public String getComparisonOperatorCode() {
			return comparisonOperatorCode;
		}
		
	}

}
