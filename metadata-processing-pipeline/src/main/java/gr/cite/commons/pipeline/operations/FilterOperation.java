package gr.cite.commons.pipeline.operations;

public class FilterOperation extends ProcessingPipelineOperation {
	private String operator;
	private String value;
	private Datatype datatype;
	
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
