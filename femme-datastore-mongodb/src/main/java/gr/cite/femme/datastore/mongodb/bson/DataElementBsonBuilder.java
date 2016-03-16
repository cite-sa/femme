package gr.cite.femme.datastore.mongodb.bson;

import java.util.List;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.core.SystemicMetadata;

public class DataElementBsonBuilder {
	private String id;
	
	private String name;
	
	private String endpoint;
	
	private List<Metadatum> metadata;
	
	private SystemicMetadata systemicMetadata;
	
	private DataElement dataElement;

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
	
	/*public DataElementBsonBuilder metadata(List<Metadatum> metadata) {
		this.metadata = metadata;
		return this;
	}*/
	
	public DataElementBsonBuilder systemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
		return this;
	}
	
	public DataElementBsonBuilder dataElement(DataElement dataElement) {
		this.dataElement = dataElement;
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
		dataElementObj.setMetadata(null);
		dataElementObj.setSystemicMetadata(systemicMetadata);
		dataElementObj.setDataElement(dataElement);
		dataElementObj.setCollections(collections);
		
		return new DataElementBson(dataElementObj);
	}
}
