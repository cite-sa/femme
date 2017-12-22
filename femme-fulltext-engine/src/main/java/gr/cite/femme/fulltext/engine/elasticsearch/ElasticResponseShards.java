package gr.cite.femme.fulltext.engine.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticResponseShards {

	@JsonProperty("total")
	private Integer total;

	@JsonProperty("successful")
	private Integer successful;

	@JsonProperty("skipped")
	private Integer skipped;

	@JsonProperty("failed")
	private Integer failed;

	@JsonProperty("failures")
	private List<ElasticResponseShardsFailure> failures;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getSuccessful() {
		return successful;
	}

	public void setSuccessful(Integer successful) {
		this.successful = successful;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

	public List<ElasticResponseShardsFailure> getFailures() {
		return failures;
	}

	public void setFailures(List<ElasticResponseShardsFailure> failures) {
		this.failures = failures;
	}
}