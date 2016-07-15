package gr.cite.femme.datastore.mongodb.metadata;

import java.util.List;

import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;

public interface MongoMetadataCollection {
	
	public void insert(Metadatum metadatum) throws MetadataStoreException;

	public Metadatum get(Metadatum metadatum) throws MetadataStoreException;

	public List<Metadatum> find(String elementId) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException;
	
	public <T extends Element> List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException;
	
	/*public <T extends Element> List<String> xPath(Metadatum metadatum, XPathEvaluator evaluator) throws MetadataStoreException;*/
	
	public void delete(Metadatum metadatum);
	
	public void deleteAll(String elementId) throws MetadataStoreException;
}
