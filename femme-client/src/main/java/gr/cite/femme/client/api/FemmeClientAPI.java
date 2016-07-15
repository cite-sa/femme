package gr.cite.femme.client;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;

public interface FemmeClient {

	public String insert(DataElement dataElement);

	public String insert(Collection collection);
	
	public String addToCollection(DataElement dataElement, String collectionId);

}