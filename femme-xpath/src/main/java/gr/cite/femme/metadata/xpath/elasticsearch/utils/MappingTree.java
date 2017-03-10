package gr.cite.femme.metadata.xpath.elasticsearch.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MappingTree extends Tree<String> {

	public String buildMapping(List<String> nestedPaths) {
		nestedPaths = nestedPaths.stream().sorted(String::compareTo).collect(Collectors.toList());
		for (String nestedPath: nestedPaths) {
			Node<String> parent = findParent(nestedPath, this.getRoot());
			Node<String> newNode = new Node<>();
			newNode.setData(nestedPath);
			parent.addChild(newNode);
		}
		return transformNodesToMappingQuery(this.getRoot());
	}

	private String transformNodesToMappingQuery(Node<String> parent) {
		String propetries = "";

		propetries += parent.getParent() == null
				? "{\"properties\" : {"
				: "\"" + parent.getData().replace(parent.getParent().getData() + ".", "") + "\": {\"type\": \"nested\"";

		if (parent.getParent() != null && parent.getChildren().size() > 0) {
			propetries += ",\"properties\": {";
		}

		propetries += parent.getChildren().stream().map(child -> transformNodesToMappingQuery(child)).collect(Collectors.joining(","));

		if (parent.getParent() == null || (parent.getParent()!= null && parent.getChildren().size() > 0)) {
			propetries += "}";
		}
		propetries += "}";

		return propetries;
	}

	private Node<String> findParent(String path, Node<String> parent) {
		for (Node<String> child: parent.getChildren()) {
			if (isChildOf(path, child.getData())) {
				return findParent(path, child);
			}
		}
		return parent;
	}

	private boolean isChildOf(String path1, String path2) {
		return path1.startsWith(path2);
	}

	public static void main(String[] args) {
		MappingTree tree = new MappingTree();
		/*Node<String> child;

		child = new Node<>();
		child.setData("a.b");
		tree.getRoot().addChild(child);

		child = new Node<>();
		child.setData("a.b.c.d");
		tree.getRoot().getChildren().get(0).addChild(child);

		child = new Node<>();
		child.setData("a.c");
		tree.getRoot().addChild(child);*/

		List<String> list = Arrays.asList("a.b", "a.b.c.d", "a.b.c.d.e", "a.c");
		tree.buildMapping(list);
	}
}
