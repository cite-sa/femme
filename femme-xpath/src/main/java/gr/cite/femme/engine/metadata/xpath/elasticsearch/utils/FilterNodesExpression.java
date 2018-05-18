package gr.cite.femme.engine.metadata.xpath.elasticsearch.utils;

import java.util.ArrayList;
import java.util.List;

public class FilterNodesExpression {
	public enum FilterNodesOperator {
		AND, OR, NONE
	}
	
	private List<FilterNode> filterNodes = new ArrayList<>();
	private FilterNodesOperator operator = FilterNodesOperator.AND;
	
	public List<FilterNode> getFilterNodes() {
		return filterNodes;
	}
	
	public void setFilterNodes(List<FilterNode> filterNodes) {
		this.filterNodes = filterNodes;
	}
	
	public FilterNodesOperator getOperator() {
		return operator;
	}
	
	public void setOperator(FilterNodesOperator operator) {
		this.operator = operator;
	}
}
