package gr.cite.femme.core;

import java.util.List;

public class DataElementBuilder {
	private String id;
	
	private String name;
	
	private String endpoint;
	
	private List<Metadatum> metadata;
	
	private List<SystemicMetadatum> systemicMetadata;
	
	private DataElement dataElement;

	private List<Collection> collections;

	
	
	public DataElementBuilder id(String id) {
		this.id = id;
		return this;
	}
	
	public DataElementBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public DataElementBuilder endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
	public DataElementBuilder metadata(List<Metadatum> metadata) {
		this.metadata = metadata;
		return this;
	}
	
	public DataElementBuilder systemicMetadata(List<SystemicMetadatum> systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
		return this;
	}
	
	public DataElementBuilder dataElement(DataElement dataElement) {
		this.dataElement = dataElement;
		return this;
	}
	
	public DataElementBuilder collections(List<Collection> collections) {
		this.collections = collections;
		return this;
	}
	
	public DataElement build() {
		DataElement dataElementObj = new DataElement();
		dataElementObj.setId(id);
		dataElementObj.setName(name);
		dataElementObj.setEndpoint(endpoint);
		dataElementObj.setMetadata(metadata);
		dataElementObj.setSystemicMetadata(systemicMetadata);
		dataElementObj.setDataElement(dataElement);
		dataElementObj.setCollections(collections);
		
		return dataElementObj;
	}
}
