package gr.cite.femme.metadata.xpath.elasticsearch.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticResponseContent {

	@JsonProperty("took")
	private Long took;

	@JsonProperty("timed_out")
	private boolean timedOut;

	@JsonProperty("_shards")
	private Map<String, Integer> shards;

	@JsonProperty("hits")
	private ElasticResponseHits hits;

	public Long getTook() {
		return took;
	}

	public void setTook(Long took) {
		this.took = took;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}

	public Map<String, Integer> getShards() {
		return shards;
	}

	public void setShards(Map<String, Integer> shards) {
		this.shards = shards;
	}

	public ElasticResponseHits getHits() {
		return hits;
	}

	public void setHits(ElasticResponseHits hits) {
		this.hits = hits;
	}
}