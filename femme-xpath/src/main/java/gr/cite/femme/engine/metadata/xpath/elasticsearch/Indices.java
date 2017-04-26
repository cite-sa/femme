package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Indices {
	private Set<String> indexMappingTypes = new HashSet<>();
	private Map<String, String> indexNames = new HashMap<>();

	public Indices() {

	}

	public Indices(List<String> fullIndexNames) {
		fullIndexNames.forEach(fullIndexName -> {
			String[] parts = fullIndexName.split("_");
			compareAndAdd(parts[0], parts[1]);
		});
	}

	public Indices(Map<String, String> fullIndexNames) {
		this.indexMappingTypes = fullIndexNames.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
		this.indexNames = fullIndexNames;
	}

	public synchronized boolean compareAndAdd(String indexMappingType, String randomId) {
		boolean notPresent = this.indexMappingTypes.add(indexMappingType);
		if (notPresent) {
			indexNames.put(indexMappingType, randomId);
		}
		return notPresent;
	}

	public synchronized Set<String> get() {
		return new HashSet<>(this.indexMappingTypes);
	}

	public synchronized String getUniqueId(String indexMappingType) {
		return this.indexNames.get(indexMappingType);
	}

	public synchronized Map<String, String> getIndicesInfo() {
		return this.indexNames;
	}


	public synchronized String getFullIndexName(String indexMappingType) {
		String typeName =  this.indexMappingTypes.stream().filter(type -> type.equals(indexMappingType)).findFirst().orElse(null);
		return typeName != null ? typeName + "_" + this.indexNames.get(typeName) : null;
	}

	public synchronized Set<String> getFullIndexNames() {
		return this.indexNames.entrySet().stream().map(entry -> entry.getKey() + "_" + entry.getValue()).collect(Collectors.toSet());
	}
}
