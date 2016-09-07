package gr.cite.femme.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.model.DataElement.DataElementBuilder;

@JsonInclude(Include.NON_EMPTY)
public class Collection extends Element {
	
	public static class CollectionBuilder {
		
		private Collection collection;
		
		public CollectionBuilder() {
			collection = new Collection();
		}
		
		public CollectionBuilder id(String id) {
			collection.setId(id);
			return this;
		}
		
		public CollectionBuilder name(String name) {
			collection.setName(name);
			return this;
		}

		public CollectionBuilder endpoint(String endpoint) {
			collection.setEndpoint(endpoint);
			return this;
		}

		public CollectionBuilder metadata(List<Metadatum> metadata) {
			collection.setMetadata(metadata);
			return this;
		}
		
		public CollectionBuilder addMetadatum(Metadatum metadatum) {
			collection.getMetadata().add(metadatum);
			return this;
		}

		public CollectionBuilder systemicMetadata(SystemicMetadata systemicMetadata) {
			collection.setSystemicMetadata(systemicMetadata);
			return this;
		}
		
		public CollectionBuilder dataElements(List<DataElement> dataElements) {
			collection.setDataElements(dataElements);
			return this;
		}
		
		public CollectionBuilder dataElement(DataElement dataElement) {
			collection.getDataElements().add(dataElement);
			return this;
		}
		
		public Collection build() {
			return collection;
		}
	}
	
	public static CollectionBuilder builder() {
		return new CollectionBuilder();
	}
	
	
	@JsonProperty
	List<DataElement> dataElements;
	
	public Collection() {
		super();
		dataElements = new ArrayList<>();
	}
	
	public Collection(String id, String name, String endpoint, List<Metadatum> metadata, SystemicMetadata systemicMetadata, List<DataElement> dataElements) {
		super(id, name, endpoint, metadata, systemicMetadata);
		if (dataElements != null) {
			this.dataElements = dataElements;
		} else {
			this.dataElements = new ArrayList<>();
		}
	}

	public List<DataElement> getDataElements() {
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
