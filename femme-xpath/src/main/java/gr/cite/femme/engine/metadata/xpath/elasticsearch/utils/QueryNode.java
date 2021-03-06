package gr.cite.femme.engine.metadata.xpath.elasticsearch.utils;

import java.util.ArrayList;
import java.util.List;

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

		public String getOperator() {
			return this.operator;
		}

		public static Operator getOperator(String operator) {
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
	
	private boolean isPredicateNode = false;
	
	private boolean isProjectionNode = false;
	private StringBuilder projectionPath = new StringBuilder();
	
	private List<FilterNode> filterNodes = new ArrayList<>();
	private FilterNodesExpression filterNodesExpression = new FilterNodesExpression();

	private List<String> metadataSchemaIds;
	private boolean isArray;
	private boolean filterPayload;

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
	
	public boolean isProjectionNode() {
		return isProjectionNode;
	}
	
	public void setProjectionNode(boolean projectionNode) {
		isProjectionNode = projectionNode;
	}
	
	public StringBuilder getProjectionPath() {
		return projectionPath;
	}
	
	public void setProjectionPath(StringBuilder projectionPath) {
		this.projectionPath = projectionPath;
	}
	
	/*public List<FilterNode> getFilterNodes() {
			return filterNodes;
		}
		
		public void setFilterNodes(List<FilterNode> filterNodes) {
			this.filterNodes = filterNodes;
		}
		*/
	public FilterNodesExpression getFilterNodesExpression() {
		return filterNodesExpression;
	}
	
	public void setFilterNodesExpression(FilterNodesExpression filterNodesExpression) {
		this.filterNodesExpression = filterNodesExpression;
	}
	
	public List<String> getMetadataSchemaIds() {
		return metadataSchemaIds;
	}

	public void setMetadataSchemaIds(List<String> metadataSchemaIds) {
		this.metadataSchemaIds = metadataSchemaIds;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean array) {
		isArray = array;
	}

	public boolean isFilterPayload() {
		return filterPayload;
	}

	public void setFilterPayload(boolean filterPayload) {
		this.filterPayload = filterPayload;
	}
}
