package gr.cite.femme.engine.metadata.xpath.elasticsearch.utils;

public class QueryTranslation {
	private Tree<QueryNode> queryTree;
	private boolean returnsValue;

	public Tree<QueryNode> getQueryTree() {
		return queryTree;
	}

	public void setQueryTree(Tree<QueryNode> queryTree) {
		this.queryTree = queryTree;
	}

	public boolean isReturnsValue() {
		return returnsValue;
	}

	public void setReturnsValue(boolean returnsValue) {
		this.returnsValue = returnsValue;
	}
}
