package gr.cite.femme.core;

import java.util.ArrayList;
import java.util.List;

public class DataElement extends Element {

	private List<DataElement> dataElements;

	private List<Collection> collections;

	public DataElement() {
		super();
		
		this.dataElements = new ArrayList<>();
		this.collections = new ArrayList<>();
	}

	public DataElement(String id, String name, String endpoint) {
		super(id, name, endpoint);
		
		this.dataElements = new ArrayList<>();
		this.collections = new ArrayList<>();
	}

	public DataElement(String id, String name, String endpoint, List<Metadatum> metadata,
			SystemicMetadata systemicMetadata, List<DataElement> dataElements, List<Collection> collections) {
		super(id, name, endpoint, metadata, systemicMetadata);
		
		if (dataElements != null) {
			this.dataElements = dataElements;
		} else {
			this.dataElements = new ArrayList<>();
		}
		
		if (collections != null) {
			this.collections = collections;
		} else {
			this.collections = new ArrayList<>();
		}
	}

	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}

	public void addCollection(Collection collection) {
		this.collections.add(collection);
	}

	public void addCollections(List<Collection> collections) {
		this.collections.addAll(collections);
	}

	@Override
	public String toString() {
		String element = super.toString();
		for (DataElement dataElement: this.dataElements) {
			element += "dataElements: {\n" + dataElement.toString() + "}";
		}
		return element;
	}
}
