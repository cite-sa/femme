package gr.cite.femme.core.datastores;

import java.util.List;

import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.exceptions.MetadataStoreException;

public interface MetadataStore {

	public void close();

	public void insert(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException;

	public Metadatum update(Metadatum metadatum) throws MetadataStoreException, MetadataIndexException;

	/*public Metadatum update(String id, Map<String, Object> fieldsAndValues) throws MetadataStoreException, MetadataIndexException;*/

	public void index(Metadatum metadatum) throws MetadataIndexException;

	public void deIndex(String id) throws MetadataIndexException;

	public void reIndexAll() throws MetadataIndexException, MetadataStoreException;
	
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException;

	public Metadatum get(Metadatum metadatum, boolean lazy) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId) throws MetadataStoreException;
	
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException;

	public List<Metadatum> find(String elementId, boolean lazy, boolean loadInactive) throws MetadataStoreException;

	public List<Metadatum> xPath(String xPath,  boolean lazyPayload) throws MetadataStoreException;

	public List<Metadatum> xPath(List<String> elementIds, String xPath, boolean lazyPayload) throws MetadataStoreException;

	public List<Metadatum> xPathInMemory(String xPath) throws MetadataStoreException;

	public List<Metadatum> xPathInMemory(List<String> elementIds, String xPath) throws MetadataStoreException;

	//public <T extends Element> T xPath(T element, String xPath) throws MetadataStoreException;
	
	public void delete(Metadatum metadatum) throws MetadataStoreException;
	
	public void deleteAll(String elementId) throws MetadataStoreException;

	public void softDelete(String metadatumId) throws MetadataStoreException, MetadataIndexException;

	public void softDeleteAll(String elementId) throws MetadataStoreException, MetadataIndexException;

	public String generateMetadatumId();

}
