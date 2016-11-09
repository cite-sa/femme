package gr.cite.femme.dto;

import java.util.List;

import gr.cite.femme.model.Collection;

public class CollectionList {
	
	private List<Collection> collections;
	
	private int size;
	
	public CollectionList() {
		
	}
	
	public CollectionList(List<Collection> collections) {
		this.collections = collections;
		this.size = collections == null ? 0 : collections.size();
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	
}
