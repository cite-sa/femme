package gr.cite.femme.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ImportEndpoint {

	@JsonProperty("endpointAlias")
	private String endpointAlias;

	@JsonProperty("endpoint")
	private String endpoint;


	public ImportEndpoint() { }

	public ImportEndpoint(String endpointAlias, String endpoint) {
		this.endpointAlias = endpointAlias;
		this.endpoint = endpoint;
	}

	public String getEndpointAlias() {
		return endpointAlias;
	}

	public void setEndpointAlias(String endpointAlias) {
		this.endpointAlias = endpointAlias;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}
