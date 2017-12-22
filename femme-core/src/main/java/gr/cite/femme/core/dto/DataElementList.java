package gr.cite.femme.core.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.cite.femme.core.model.DataElement;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DataElementList {

	@JsonProperty("dataElements")
	private List<DataElement> elements;

	@JsonProperty("size")
	private int size;

	public DataElementList() { }

	public DataElementList(List<DataElement> elements) {
		this.elements = elements;
		this.size = elements.size();
	}

	public List<DataElement> getElements() {
		return elements;
	}

	public void setElements(List<DataElement> elements) {
		this.elements = elements;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		this.size = size;
	}



	
}
