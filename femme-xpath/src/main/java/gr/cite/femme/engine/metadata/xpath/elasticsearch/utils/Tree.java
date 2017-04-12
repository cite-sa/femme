package gr.cite.femme.engine.metadata.xpath.elasticsearch.utils;

public class Tree<T> {

	private Node<T> root;

	private int totalLevels;

	public Tree() {
		this.root = new Node<>();
		this.totalLevels = 1;
	}

	public Node<T> getRoot() {
		return root;
	}

	public void setRoot(Node<T> root) {
		this.root = root;
	}

	/*public List<Node<T>> getLevel(int level) {
		if (level >= totalLevels) {
			throw new IllegalArgumentException("Query tree level " + level + "doesn't exist.");
		}

		List<Node<T>> levelNodes = new ArrayList<>();
		levelNodes.add(root);
		for (int i = 0; i < level; i++) {
			levelNodes = levelNodes.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
		}
		return levelNodes;
	}*/


}
