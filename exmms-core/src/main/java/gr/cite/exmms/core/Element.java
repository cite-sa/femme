package gr.cite.exmms.core;

import java.util.List;

public class Element {

	private String id;
	
	private String endpoint;

	private List<DataElementMetadatum> metadata;

	private List<SystemicMetadatum> systemicMetadata;
	
	private List<DataElement> dataElements;

	
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

	public List<DataElementMetadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<DataElementMetadatum> metadata) {
		this.metadata = metadata;
	}
	
	public List<SystemicMetadatum> getSystemicMetadata() {
		return systemicMetadata;
	}

	public void setSystemicMetadata(List<SystemicMetadatum> systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
	}

	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}
}