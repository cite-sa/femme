package gr.cite.femme.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.cite.femme.model.DataElement;

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
