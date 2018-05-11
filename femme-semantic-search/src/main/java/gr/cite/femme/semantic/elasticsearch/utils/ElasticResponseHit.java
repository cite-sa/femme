package gr.cite.femme.semantic.elasticsearch.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticResponseHit<T> {

	@JsonProperty("_index")
	private String index;

	@JsonProperty("_type")
	private String type;

	@JsonProperty("_id")
	private String id;

	@JsonProperty("_score")
	private double score;

	@JsonProperty("_source")
	private T source;

	@JsonProperty("sort")
	private List<Integer> sort;

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public T getSource() {
		return source;
	}

	public void setSource(T source) {
		this.source = source;
	}

	public List<Integer> getSort() {
		return sort;
	}

	public void setSort(List<Integer> sort) {
		this.sort = sort;
	}
}