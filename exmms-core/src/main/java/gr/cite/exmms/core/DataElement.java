package gr.cite.exmms.core;

import java.util.List;

public class DataElement {

	String id;

	List<DataElementMetadatum> matadata;

	List<SystemicMetadatum> systemicMetadata;

	List<Collection> collections;

	public List<SystemicMetadatum> getSystemicMetadata() {
		return systemicMetadata;
	}

	public void setSystemicMetadata(List<SystemicMetadatum> systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<DataElementMetadatum> getMatadata() {
		return matadata;
	}

	public void setMatadata(List<DataElementMetadatum> matadata) {
		this.matadata = matadata;
	}

}
