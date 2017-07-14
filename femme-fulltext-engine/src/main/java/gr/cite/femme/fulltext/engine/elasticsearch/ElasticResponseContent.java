package gr.cite.femme.fulltext.engine.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticResponseContent {

	@JsonProperty("_scroll_id")
	private String scrollId;

	@JsonProperty("took")
	private Long took;

	@JsonProperty("timed_out")
	private boolean timedOut;

	@JsonProperty("_shards")
	private ElasticResponseShards shards;

	@JsonProperty("terminated_early")
	private boolean terminatedEarly;

	@JsonProperty("hits")
	private ElasticResponseHits hits;


	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

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

	public ElasticResponseShards getShards() {
		return shards;
	}

	public void setShards(ElasticResponseShards shards) {
		this.shards = shards;
	}

	public boolean isTerminatedEarly() {
		return terminatedEarly;
	}

	public void setTerminatedEarly(boolean terminatedEarly) {
		this.terminatedEarly = terminatedEarly;
	}

	public ElasticResponseHits getHits() {
		return hits;
	}

	public void setHits(ElasticResponseHits hits) {
		this.hits = hits;
	}
}