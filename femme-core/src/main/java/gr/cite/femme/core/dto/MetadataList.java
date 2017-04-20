package gr.cite.femme.core.dto;

import gr.cite.femme.core.model.Metadatum;

import java.util.List;

public class MetadataList {

	private List<Metadatum> metadata;

	private int size;

	public MetadataList() {

	}

	public MetadataList(List<Metadatum> metadata) {
		this.metadata = metadata;
		this.size = metadata == null ? 0 : metadata.size();
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
