package gr.cite.femme.client.api;

import gr.cite.femme.model.Metadatum;

public interface MetadataIndexClient {
	
public void index(Metadatum metadatum);
	
	public void reIndex(Metadatum metadatum);
	
	public void reIndex();
	
	public String search(String xPath); 
}
