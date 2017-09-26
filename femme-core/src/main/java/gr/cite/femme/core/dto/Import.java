package gr.cite.femme.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Import {

	@JsonProperty("id")
	private String id;

	@JsonProperty("endpoint")
	private String endpoint;

	@JsonProperty("endpointAlias")
	private String endpointAlias;

	@JsonProperty("collectionId")
	private String collectionId;

	@JsonProperty("existingDataElements")
	private List<String> existingDataElements = new ArrayList<>();

	@JsonProperty("newDataElements")
	private List<String> newDataElements = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpointAlias() {
		return endpointAlias;
	}

	public void setEndpointAlias(String endpointAlias) {
		this.endpointAlias = endpointAlias;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public List<String> getExistingDataElements() {
		return existingDataElements;
	}

	public void setExistingDataElements(List<String> existingDataElements) {
		this.existingDataElements = existingDataElements;
	}

	public List<String> getNewDataElements() {
		return newDataElements;
	}

	public void setNewDataElements(List<String> newDataElements) {
		this.newDataElements = newDataElements;
	}
}
