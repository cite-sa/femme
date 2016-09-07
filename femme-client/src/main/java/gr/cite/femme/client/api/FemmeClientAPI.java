package gr.cite.femme.client.api;

import java.util.List;

import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public interface FemmeClientAPI {

	public String insert(DataElement dataElement) throws FemmeDatastoreException;

	public String insert(Collection collection) throws FemmeDatastoreException;
	
	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeDatastoreException;
	
	
	public List<Collection> getCollections() throws FemmeDatastoreException;
	
	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public List<Collection> findCollections(QueryClient query, Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public Collection getCollectionById(String id) throws FemmeDatastoreException;
	
	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException;
	
	public List<Collection> getCollectionByName(String name) throws FemmeDatastoreException;
	
	
	public List<DataElement> getDataElements() throws FemmeDatastoreException;
	
	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public List<DataElement> findDataElements(QueryClient query, Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public List<DataElement> getDataElements(String collectionId) throws FemmeDatastoreException;
	
	public List<DataElement> getDataElements(String collectionId, Integer limit, Integer offset) throws FemmeDatastoreException;
	
	public DataElement getDataElementById(String id) throws FemmeDatastoreException;
	
	public List<DataElement> getDataElementByEndpoint(String endpoint) throws FemmeDatastoreException;
	
	public List<DataElement> getDataElementByName(String name) throws FemmeDatastoreException;
	
}