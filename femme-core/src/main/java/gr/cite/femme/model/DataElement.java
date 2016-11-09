package gr.cite.femme.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class DataElement extends Element {
	
	@JsonProperty
	private List<DataElement> dataElements;

	@JsonProperty
	private List<Collection> collections;

	public DataElement() {
		super();
		
		this.dataElements = new ArrayList<>();
		this.collections = new ArrayList<>();
	}

	/*public DataElement(String id, String name, String endpoint) {
		super(id, name, endpoint);
		
		this.dataElements = new ArrayList<>();
		this.collections = new ArrayList<>();
	}*/

	/*public DataElement(String id, String name, String endpoint, List<Metadatum> metadata,
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
	}*/
	
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
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private DataElement dataElement;
		
		public Builder() {
			dataElement = new DataElement();
		}
		
		public Builder id(String id) {
			dataElement.setId(id);
			return this;
		}
		
		public Builder name(String name) {
			dataElement.setName(name);
			return this;
		}

		public Builder endpoint(String endpoint) {
			dataElement.setEndpoint(endpoint);
			return this;
		}

		public Builder metadata(List<Metadatum> metadata) {
			dataElement.setMetadata(metadata);
			return this;
		}
		
		public Builder metadatum(Metadatum metadatum) {
			dataElement.getMetadata().add(metadatum);
			return this;
		}

		public Builder systemicMetadata(SystemicMetadata systemicMetadata) {
			dataElement.setSystemicMetadata(systemicMetadata);
			return this;
		}
		
		public Builder dataElements(List<DataElement> dataElements) {
			dataElement.setDataElements(dataElements);
			return this;
		}
		
		public Builder dataElement(DataElement dataElement) {
			dataElement.getDataElements().add(dataElement);
			return this;
		}
		
		public Builder collections(List<Collection> collections) {
			dataElement.setCollections(collections);
			return this;
		}
		
		public Builder collection(Collection collection) {
			dataElement.getCollections().add(collection);
			return this;
		}
		
		public DataElement build() {
			return dataElement;
		}
	}
	
}
