package gr.cite.femme.engine.metadata.xpath.elasticsearch.utils;

public class FilterNode {
	private StringBuilder filterPath = new StringBuilder();
	private QueryNode.Operator operator;
	private String value;
	
	public StringBuilder getFilterPath() {
		return filterPath;
	}
	
	public void setFilterPath(StringBuilder filterPath) {
		this.filterPath = filterPath;
	}
	
	public QueryNode.Operator getOperator() {
		return operator;
	}
	
	public void setOperator(QueryNode.Operator operator) {
		this.operator = operator;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
