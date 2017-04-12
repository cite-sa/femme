package gr.cite.femme.core.dto;

import java.util.List;

import gr.cite.femme.core.model.DataElement;

/*@JsonInclude(Include.NON_EMPTY)*/
public class DataElementList {
	
	/*@JsonProperty*/
	private List<DataElement> dataElements;
	
	/*@JsonProperty*/
	private int size;

	public DataElementList() {
	}
	
	public DataElementList(List<DataElement> dataElements) {
		this.dataElements = dataElements;
		this.size = dataElements.size();
	}
	
	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	
	
	
}
