package gr.cite.femme.client.api;

import java.util.List;

import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;

public interface FemmeClientAPI {

	public String insert(Collection collection) throws FemmeDatastoreException;

	public String insert(DataElement dataElement) throws FemmeDatastoreException;

	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeDatastoreException;
	

	public List<Collection> getCollections() throws FemmeDatastoreException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset, String asc, String desc, List<String> include,
			List<String> exclude, String xPath) throws FemmeDatastoreException, FemmeClientException;

	public <T extends Criterion> List<Collection> findCollections(Query<T> query, Integer limit, Integer offset,
			String asc, String desc, List<String> include, List<String> exclude, String xPath)
			throws FemmeDatastoreException, FemmeClientException;

	public Collection getCollectionById(String id) throws FemmeDatastoreException;

	public Collection getCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException;

	public Collection getCollectionByName(String name) throws FemmeDatastoreException, FemmeClientException;
	

	public List<DataElement> getDataElements() throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset, String asc, String desc,
			List<String> include, List<String> exclude, String xPath) throws FemmeDatastoreException, FemmeClientException;

	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, Integer limit, Integer offset,
			String asc, String desc, List<String> include, List<String> exclude, String xPath)
			throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeDatastoreException;

	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset)
			throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset)
			throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsByIdInCollectionById(String collectionId, String dataElementId) throws FemmeDatastoreException, FemmeClientException;
	
	public DataElement getDataElementById(String id) throws FemmeDatastoreException;

	public DataElement getDataElementByEndpoint(String endpoint) throws FemmeDatastoreException, FemmeClientException;

	public List<DataElement> getDataElementsByName(String name) throws FemmeDatastoreException, FemmeClientException;

}