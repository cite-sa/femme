package gr.cite.femme.metadata.xpath.elasticsearch.utils;

public class QueryNode {

	public enum Operator {
		EQUALS("="),
		NOT_EQUALS("!="),
		GREATER_THAN(">"),
		GREATER_EQUAL_THAN(">="),
		LESS_THAN("<"),
		LESS_EQUAL_THAN("<=");

		private String operator;

		private Operator(String operator) {
			this.operator = operator;
		}

		public String getOperationString() {
			return this.operator;
		}

		public static Operator getOperationEnum(String operator) {
			switch (operator) {
				case "=":
					return Operator.EQUALS;
				case "=!":
					return Operator.NOT_EQUALS;
				case ">":
					return Operator.GREATER_THAN;
				case ">=":
					return Operator.GREATER_EQUAL_THAN;
				case "<":
					return Operator.LESS_THAN;
				case "<=":
					return Operator.LESS_EQUAL_THAN;
				default:
					return null;
			}
		}
	}

	private StringBuilder nodePath = new StringBuilder();

	private StringBuilder filterPath = new StringBuilder();

	private Operator operator;

	private String value;

	public QueryNode() {

	}

	public QueryNode(QueryNode queryNode) {
		this.nodePath = queryNode.getNodePath();
	}

	public StringBuilder getNodePath() {
		return nodePath;
	}

	public void setNodePath(StringBuilder nodePath) {
		this.nodePath = nodePath;
	}

	public StringBuilder getFilterPath() {
		return filterPath;
	}

	public void setFilterPath(StringBuilder filterPath) {
		this.filterPath = filterPath;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
