package gr.cite.femme.core;

import java.util.ArrayList;
import java.util.List;

public class Collection extends Element {
	List<DataElement> dataElements;
	
	public Collection() {
		super();
		dataElements = new ArrayList<>();
	}
	
	public Collection(String id, String name, String endpoint, List<Metadatum> metadata, String systemicMetadata, List<DataElement> dataElements) {
		super(id, name, endpoint, metadata);
		if (dataElements != null) {
			this.dataElements = dataElements;
		} else {
			this.dataElements = new ArrayList<>();
		}
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
}
