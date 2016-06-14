package gr.cite.femme.datastore.mongodb.metadata;

import java.util.List;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;

public interface MongoMetadataCollection {
	
	public String insert(Metadatum metadatum) throws MetadataStoreException;

	public Metadatum get(String metadatumId) throws MetadataStoreException;

	public List<Metadatum> find(String elementId) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException;
	
	public <T extends Element> List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException;
	
	/*public <T extends Element> List<String> xPath(Metadatum metadatum, XPathEvaluator evaluator) throws MetadataStoreException;*/
	
	public void delete(Metadatum metadatum);
	
	public void deleteAll(String elementId) throws MetadataStoreException;
}
