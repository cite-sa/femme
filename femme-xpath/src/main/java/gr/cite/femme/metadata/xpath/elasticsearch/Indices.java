package gr.cite.femme.metadata.xpath.elasticsearch;

import java.util.HashSet;
import java.util.Set;

public class Indices {
	private Set<String> indexMappingTypes = new HashSet<>();
	private Set<String> indexNames = new HashSet<>();

	public synchronized boolean compareAndAdd(String indexName) {

		return this.indexMappingTypes.add(indexName);

	}

	public synchronized Set<String> get() {
		return new HashSet<>(this.indexMappingTypes);
	}

	public synchronized Set<String> getFullIndexNames() {
		return new HashSet<>(this.indexNames);
	}
}
