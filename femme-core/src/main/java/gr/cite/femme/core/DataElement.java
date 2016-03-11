package gr.cite.femme.core;

import java.util.ArrayList;
import java.util.List;

public class DataElement extends Element {
	
	private DataElement dataElement;

	private List<Collection> collections;
	
	public DataElement(String id, String name, String endpoint) {
		super(id, name, endpoint);
		collections = new ArrayList<>();
	}
	
	public DataElement() {
		super();
		collections = new ArrayList<>();
	}
	
	public DataElement(String id, String name, String endpoint, List<Metadatum> metadata, String systemicMetadata, DataElement dataElement, List<Collection> collections) {
		super(id, name, endpoint, metadata);
		if (dataElement != null) {
			this.dataElement = dataElement;
		}
		if (collections != null) {
			this.collections = collections;
		} else {
			this.collections = new ArrayList<>();
		}
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
	
	@Override
	public String toString() {
		String element = super.toString();
		if (this.dataElement != null) {
			element += "dataElement: {\n" + this.dataElement.toString() + "}";
		}
		return element;
	}
}
