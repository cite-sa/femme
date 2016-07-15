package gr.cite.femme.model;

import java.util.List;

public class DataElementList {
	private List<DataElement> dataElements;

	public DataElementList() {
	}
	
	public DataElementList(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}
	
	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}
	
	
}
