package gr.cite.exmms.manager.core;

import java.util.List;

public class DataElement {

	String id;

	List<DataElementMetadatum> matadata;
	
	List<SystemicMetadatum> systemicMetadata;

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
