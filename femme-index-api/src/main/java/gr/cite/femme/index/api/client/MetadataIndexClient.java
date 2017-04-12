package gr.cite.femme.index.api.client;

import gr.cite.femme.core.model.Metadatum;

public interface MetadataIndexClient {
	
public void index(Metadatum metadatum);
	
	public void reIndex(Metadatum metadatum);
	
	public void reIndex();
	
	public String search(String xPath); 
}
