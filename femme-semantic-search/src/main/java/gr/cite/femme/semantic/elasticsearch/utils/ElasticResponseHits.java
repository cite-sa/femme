package gr.cite.femme.semantic.elasticsearch.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticResponseHits<T> {

	@JsonProperty("total")
	private int total;

	@JsonProperty("max_score")
	private double maxScore;

	@JsonProperty("hits")
	private List<ElasticResponseHit<T>> hits;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}

	public List<ElasticResponseHit<T>> getHits() {
		return hits;
	}

	public void setHits(List<ElasticResponseHit<T>> hits) {
		this.hits = hits;
	}
}