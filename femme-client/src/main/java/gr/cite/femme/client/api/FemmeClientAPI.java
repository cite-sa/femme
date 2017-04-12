package gr.cite.femme.client.api;

import java.util.List;

import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;

public interface FemmeClientAPI {

	public String beginImport(String endpoint) throws FemmeDatastoreException;

	public void endImport(String importId) throws FemmeDatastoreException;

	public String importCollection(String importId, Collection collection) throws FemmeDatastoreException;

	public String importInCollection(String importId, DataElement dataElement) throws FemmeDatastoreException;

	public String insert(Collection collection) throws FemmeDatastoreException;

	public String insert(DataElement dataElement) throws FemmeDatastoreException;

	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeDatastoreException;
	

	public List<Collection> getCollections() throws FemmeDatastoreException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;

	public <T extends Criterion> List<Collection> findCollections(Query<T> query, QueryOptionsMessenger options, String xPath)
			throws FemmeDatastoreException, FemmeClientException;
	
	public Collection getCollectionById(String id) throws FemmeDatastoreException;

	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException;

	public Collection getCollectionByName(String name) throws FemmeDatastoreException, FemmeClientException;
	

	public List<DataElement> getDataElements() throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset, String xPath) throws FemmeDatastoreException, FemmeClientException;

	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath)
			throws FemmeDatastoreException, FemmeClientException;
	
	public DataElement getDataElementById(String id) throws FemmeDatastoreException;

	public List<DataElement> getDataElementsByName(String name) throws FemmeDatastoreException, FemmeClientException;
	

	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset)
			throws FemmeDatastoreException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset)
			throws FemmeDatastoreException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByName(String endpoint) throws FemmeDatastoreException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByName(String endpoint, Integer limit, Integer offset)
			throws FemmeDatastoreException, FemmeClientException;
	
}