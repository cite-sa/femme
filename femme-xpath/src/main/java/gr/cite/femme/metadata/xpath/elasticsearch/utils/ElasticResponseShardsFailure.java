package gr.cite.femme.metadata.xpath.elasticsearch.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ElasticResponseShardsFailure {

	@JsonProperty("shard")
	private Integer shard;

	@JsonProperty("index")
	private String index;

	@JsonProperty("node")
	private String node;

	@JsonProperty("reason")
	private Map<String, Object> reason;

	public Integer getShard() {
		return shard;
	}

	public void setShard(Integer shard) {
		this.shard = shard;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Map<String, Object> getReason() {
		return reason;
	}

	public void setReason(Map<String, Object> reason) {
		this.reason = reason;
	}
}