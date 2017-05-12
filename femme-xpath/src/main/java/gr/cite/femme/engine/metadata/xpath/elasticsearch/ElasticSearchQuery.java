package gr.cite.femme.engine.metadata.xpath.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ElasticSearchQuery {

	private Map<String, List<String>> indicesPerQuery;

	private List<String> indices;
	private List<String> queries;

	private Set<String> includes;

	public ElasticSearchQuery() {
		this.queries = new ArrayList<>();
		this.indicesPerQuery = new HashMap<>();
		this.includes = new HashSet<>();
	}

	public ElasticSearchQuery(List<String> indices, List<String> queries) {
		this.indices = indices;
		this.queries = queries;
	}

	public ElasticSearchQuery(ElasticSearchQuery elasticSearchQuery) {
		this.indices = elasticSearchQuery.getIndices();
		this.queries = elasticSearchQuery.getQueries();
	}

	public List<String> getIndices() {
		return indices;
	}

	public void setIndices(List<String> indices) {
		this.indices = indices;
	}

	public List<String> getQueries() {
		return queries;
	}

	public void setQueries(List<String> queries) {
		this.queries = queries;
	}

	public Map<String, List<String>> getIndicesPerQuery() {
		return indicesPerQuery;
	}

	public void setIndicesPerQuery(Map<String, List<String>> indicesPerQuery) {
		this.indicesPerQuery = indicesPerQuery;
	}

	public void addQuery(String query, List<String> indices) {
		this.queries.add(query);
		if (this.indicesPerQuery.containsKey(query)) {
			this.indicesPerQuery.get(query).addAll(indices);
		} else {
			this.indicesPerQuery.put(query, indices);
		}
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public void setIncludes(Set<String> includes) {
		this.includes = includes;
	}

	public String build() {
		return this.queries.stream().collect(Collectors.joining(","));
	}
}
