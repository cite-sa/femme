package gr.cite.femme.datastore.mongodb.bson;

import java.util.List;

import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.model.SystemicMetadata;

public class ElementBsonBuilder {
	protected String id;
	
	protected String name;
	
	protected String endpoint;
	
	protected List<Metadatum> metadata;
	
	protected SystemicMetadata systemicMetadata;
	
	
	public ElementBsonBuilder id(String id) {
		this.id = id;
		return this;
	}
	
	public ElementBsonBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public ElementBsonBuilder endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
	public ElementBsonBuilder metadata(List<Metadatum> metadata) {
		this.metadata = metadata;
		return this;
	}
	
	public ElementBsonBuilder systemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
		return this;
	}
	
	public ElementBson build() {
		Element elementObj = new Element();
		elementObj.setId(id);
		elementObj.setName(name);
		elementObj.setEndpoint(endpoint);
		elementObj.setMetadata(metadata);
		elementObj.setSystemicMetadata(systemicMetadata);
		
		return new ElementBson(elementObj);
	}
}
