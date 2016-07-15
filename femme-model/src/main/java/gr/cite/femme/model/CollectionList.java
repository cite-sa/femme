package gr.cite.femme.model;

import java.util.List;

public class CollectionList {
	private List<Collection> collections;
	
	public CollectionList() {
	}
	
	public CollectionList(List<Collection> collections) {
		this.collections = collections;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}
}
