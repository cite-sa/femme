package gr.cite.femme.core.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class Collection extends Element {
	
	@JsonProperty
	private List<DataElement> dataElements;
	
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
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Collection collection;
		
		private Builder() {
			collection = new Collection();
		}
		
		public Builder id(String id) {
			collection.setId(id);
			return this;
		}
		
		public Builder name(String name) {
			collection.setName(name);
			return this;
		}

		public Builder endpoint(String endpoint) {
			collection.setEndpoint(endpoint);
			return this;
		}

		public Builder metadata(List<Metadatum> metadata) {
			collection.setMetadata(metadata);
			return this;
		}
		
		public Builder metadatum(Metadatum metadatum) {
			collection.getMetadata().add(metadatum);
			return this;
		}

		public Builder systemicMetadata(SystemicMetadata systemicMetadata) {
			collection.setSystemicMetadata(systemicMetadata);
			return this;
		}
		
		/*public Builder dataElements(List<DataElement> dataElements) {
			collection.setDataElements(dataElements);
			return this;
		}
		
		public Builder dataElement(DataElement dataElement) {
			collection.getDataElements().add(dataElement);
			return this;
		}*/
		
		public Collection build() {
			return collection;
		}
	}
	
}
