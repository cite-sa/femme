package gr.cite.femme.metadata.xpath.elasticsearch.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryTree<T> {

	private int totalLevels;

	private Map<Integer, List<T>> levels;

	public QueryTree() {
		this.levels = new HashMap<>();
		this.levels.put(0, null);
		this.totalLevels = 0;
	}

	public List<T> getLevel(int level) {
		return levels.get(level);
	}

	public void addLevel(List<T> nodes) {
		levels.put(totalLevels, nodes);
		totalLevels ++;
	}

	public void addNode(T node, int level) {
		if (level == totalLevels + 1) {
			List<T> nodes = new ArrayList<>();
			nodes.add(node);
			levels.put(level, nodes);
		} else if (level <= totalLevels) {
			levels.get(level).add(node);
		} else {
			throw new IllegalArgumentException("Level " + level + " exceeds query tree max level");
		}

		levels.get(level).add(node);
	}

}
