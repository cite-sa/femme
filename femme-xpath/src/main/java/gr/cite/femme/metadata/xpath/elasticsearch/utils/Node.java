package gr.cite.femme.metadata.xpath.elasticsearch.utils;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {

	private Node<T> parent = null;

	private List<Node<T>> children = new ArrayList<>();

	private T data;

	public Node<T> getParent() {
		return parent;
	}

	public void setParent(Node<T> parent) {
		this.parent = parent;
	}

	public List<Node<T>> getChildren() {
		return children;
	}

	public void setChildren(List<Node<T>> children) {
		this.children = children;
	}

	public void addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}
}
