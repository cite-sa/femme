package gr.cite.femme.datastore.mongodb.bson;

import java.util.List;

import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.model.SystemicMetadata;

public class DataElementBsonBuilder {
	private String id;
	
	private String name;
	
	private String endpoint;
	
	private List<Metadatum> metadata;
	
	private SystemicMetadata systemicMetadata;
	
	private List<DataElement> dataElements;

	private List<Collection> collections;

	
	
	public DataElementBsonBuilder id(String id) {
		this.id = id;
		return this;
	}
	
	public DataElementBsonBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public DataElementBsonBuilder endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
	public DataElementBsonBuilder metadata(List<Metadatum> metadata) {
		this.metadata = metadata;
		return this;
	}
	
	public DataElementBsonBuilder systemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
		return this;
	}
	
	public DataElementBsonBuilder dataElements(List<DataElement> dataElements) {
		this.dataElements = dataElements;
		return this;
	}
	
	public DataElementBsonBuilder collections(List<Collection> collections) {
		this.collections = collections;
		return this;
	}
	
	public DataElementBson build() {
		DataElement dataElementObj = new DataElement();
		dataElementObj.setId(id);
		dataElementObj.setName(name);
		dataElementObj.setEndpoint(endpoint);
		dataElementObj.setMetadata(metadata);
		dataElementObj.setSystemicMetadata(systemicMetadata);
		dataElementObj.setDataElements(dataElements);
		dataElementObj.setCollections(collections);
		
		return new DataElementBson(dataElementObj);
	}
}
