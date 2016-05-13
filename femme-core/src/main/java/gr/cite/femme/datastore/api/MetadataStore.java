package gr.cite.femme.datastore.api;

import java.util.List;

import org.bson.types.ObjectId;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;

public interface MetadataStore {
	public ObjectId insert(Metadatum metadatum, String elementId) throws MetadataStoreException ;
	
	public Metadatum get(String fileId) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException;
	
	public <T extends Element> List<T> find(List<T> elementIds, String xPath) throws MetadataStoreException;
	
	public List<Metadatum> find(Metadatum metadatum);
	
	public List<Metadatum> find(List<Metadatum> metadataList) throws MetadataStoreException;
	
	public Metadatum xPath(Metadatum metadatum, String xPath) throws MetadataStoreException;
	
	public void delete(String elementId) throws MetadataStoreException;
}
