package gr.cite.exmms.core;

import java.util.ArrayList;
import java.util.List;

public class Collection extends Element {
	List<DataElement> dataElements;
	
	public Collection() {
		super();
		dataElements = new ArrayList<>();
	}

	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}
}
