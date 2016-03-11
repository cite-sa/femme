package gr.cite.femme.core;

import java.util.ArrayList;
import java.util.List;

public class Element {

	private String id;
	
	private String name;
	
	private String endpoint;

	private List<Metadatum> metadata;

	private List<SystemicMetadatum> systemicMetadata;
	
	public Element() {
		metadata = new ArrayList<>();
		systemicMetadata = new ArrayList<>();
	}
	
	public Element(String id, String name, String endpoint) {
		this.id = id;
		this.name = name;
		this.endpoint = endpoint;
		this.metadata = new ArrayList<>();
		this.systemicMetadata = new ArrayList<>();
	}
	
	public Element(String id, String name, String endpoint, List<Metadatum> metadata) {
		this.id = id;
		this.name = name;
		this.endpoint = endpoint;
		this.metadata = metadata;
		this.systemicMetadata = new ArrayList<>();
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
	
	public List<SystemicMetadatum> getSystemicMetadata() {
		return systemicMetadata;
	}

	public void setSystemicMetadata(List<SystemicMetadatum> systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
	}
	@Override
	public String toString() {
		StringBuilder elementBuilder = new StringBuilder();
		elementBuilder.append("ID: " + this.id);
		elementBuilder.append("\n");
		elementBuilder.append("name: " + this.name);
		elementBuilder.append("\n");
		elementBuilder.append("endpoint: " + this.endpoint);
		elementBuilder.append("\n");
		if (this.metadata != null) {
			elementBuilder.append("metadata: [\n");
			for (Metadatum metadatum : this.metadata) {
				elementBuilder.append(metadatum.toString());
			}
			elementBuilder.append("]\n");
		}
		elementBuilder.append("\n");
		return elementBuilder.toString();
	}
}