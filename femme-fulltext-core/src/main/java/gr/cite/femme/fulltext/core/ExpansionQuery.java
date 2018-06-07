package gr.cite.femme.fulltext.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExpansionQuery {
	
	@JsonProperty("direction")
	private String direction;
	
	@JsonProperty("maxBroader")
	private Integer maxBroader = 5;
	
	@JsonProperty("maxNarrower")
	private Integer maxNarrower = 5;
	
	public String getDirection() {
		return direction;
	}
	
	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public Integer getMaxBroader() {
		return maxBroader;
	}
	
	public void setMaxBroader(Integer maxBroader) {
		this.maxBroader = maxBroader;
	}
	
	public Integer getMaxNarrower() {
		return maxNarrower;
	}
	
	public void setMaxNarrower(Integer maxNarrower) {
		this.maxNarrower = maxNarrower;
	}
}
