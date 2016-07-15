package gr.cite.femme.client.api;

import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public interface FemmeClientAPI {

	public String insert(DataElement dataElement);

	public String insert(Collection collection);
	
	public String addToCollection(DataElement dataElement, String collectionId);

}