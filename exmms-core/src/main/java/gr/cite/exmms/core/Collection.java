package gr.cite.exmms.core;

import java.util.List;

public class Collection extends DataElement {
	private String endpoint;

	private List<DataElement> dataElements;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}

}
