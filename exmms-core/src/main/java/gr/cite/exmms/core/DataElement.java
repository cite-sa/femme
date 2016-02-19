package gr.cite.exmms.core;

import java.util.ArrayList;
import java.util.List;

public class DataElement extends Element {
	
	private DataElement dataElement;

	private List<Collection> collections;
	
	public DataElement() {
		super();
		collections = new ArrayList<>();
	}
	
	public DataElement getDataElement() {
		return dataElement;
	}

	public void setDataElement(DataElement dataElement) {
		this.dataElement = dataElement;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}
}
