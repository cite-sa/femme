package gr.cite.femme.datastore.mongodb.bson;

import java.util.List;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.core.SystemicMetadata;

public class CollectionBsonBuilder {
	private String id;
	
	private String name;
	
	private String endpoint;
	
	private List<Metadatum> metadata;
	
	private SystemicMetadata systemicMetadata;
	
	private List<DataElement> dataElements;
	
	
	public CollectionBsonBuilder id(String id) {
		this.id = id;
		return this;
	}
	
	public CollectionBsonBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public CollectionBsonBuilder endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
	public CollectionBsonBuilder metadata(List<Metadatum> metadata) {
		this.metadata = metadata;
		return this;
	}
	
	public CollectionBsonBuilder systemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
		return this;
	}
	
	public CollectionBsonBuilder dataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
		return this;
	}
	
	public CollectionBson build() {
		Collection collectionObj = new Collection();
		collectionObj.setId(id);
		collectionObj.setName(name);
		collectionObj.setEndpoint(endpoint);
		collectionObj.setMetadata(metadata);
		collectionObj.setSystemicMetadata(systemicMetadata);
		collectionObj.setDataElements(dataElements);
		
		return new CollectionBson(collectionObj);
	}
}
