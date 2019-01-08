package gr.cite.pipelinenew.step.filter;

import gr.cite.pipelinenew.step.Datatype;

public class FilterCondition {
	private String field;
	private String operator;
	private String value;
	private Datatype datatype;
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Datatype getDatatype() {
		return datatype;
	}
	
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}
}
