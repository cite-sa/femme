package gr.cite.femme.index.api.client;

import gr.cite.femme.core.model.Metadatum;

import javax.xml.stream.XMLStreamException;

public interface MetadataIndexClient {
	
public void index(Metadatum metadatum) throws XMLStreamException;
	
	public void reIndex(Metadatum metadatum);
	
	public void reIndex();
	
	public String search(String xPath); 
}
