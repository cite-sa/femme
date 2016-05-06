package gr.cite.femme.core;

import java.util.ArrayList;
import java.util.List;

public class Element {

	private String id;
	
	private String name;
	
	private String endpoint;

	private List<Metadatum> metadata;

	private SystemicMetadata systemicMetadata;
	
	public Element() {
		metadata = new ArrayList<>();
		/*systemicMetadata = new SystemicMetadata();*/
	}
	
	public Element(String id, String name, String endpoint) {
		this.id = id;
		this.name = name;
		this.endpoint = endpoint;
		this.metadata = new ArrayList<>();
		systemicMetadata = new SystemicMetadata();
	}
	
	public Element(String id, String name, String endpoint, List<Metadatum> metadata) {
		this.id = id;
		this.name = name;
		this.endpoint = endpoint;
		this.metadata = metadata;
		systemicMetadata = new SystemicMetadata();
	}
	
	public Element(String id, String name, String endpoint, List<Metadatum> metadata, SystemicMetadata systemicMetadata) {
		this.id = id;
		this.name = name;
		this.endpoint = endpoint;
		if (metadata != null) {
			this.metadata = metadata;			
		} else {
			this.metadata = new ArrayList<>();
		}
		if (systemicMetadata != null) {
			this.systemicMetadata = systemicMetadata;			
		} else {
			systemicMetadata = new SystemicMetadata();
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}
	
	public SystemicMetadata getSystemicMetadata() {
		return systemicMetadata;
	}

	public void setSystemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
	}
	@Override
	public String toString() {
		StringBuilder elementBuilder = new StringBuilder();
		if (this.id != null) {
			elementBuilder.append("ID: " + this.id);			
		}
		elementBuilder.append("\n");
		if (this.name != null) {
			elementBuilder.append("name: " + this.name);			
		}
		elementBuilder.append("\n");
		if (this.endpoint != null) {
			elementBuilder.append("endpoint: " + this.endpoint);			
		}
		elementBuilder.append("\n");
		if (this.metadata != null) {
			elementBuilder.append("metadata: [\n");
			for (Metadatum metadatum : this.metadata) {
				elementBuilder.append(metadatum.toString());
			}
			elementBuilder.append("]\n");
		}
		if (this.systemicMetadata != null) {
			elementBuilder.append("created: " + this.systemicMetadata.getCreated().toString());
			elementBuilder.append("\n");
			elementBuilder.append("modified: " + this.systemicMetadata.getModified().toString());
		}
		elementBuilder.append("\n");
		return elementBuilder.toString();
	}
}