package gr.cite.femme.client.api;

import java.util.List;
import java.util.Set;

import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.dto.QueryOptionsMessenger;

public interface FemmeClientAPI {

	public String beginImport(String endpointAlias, String endpoint) throws FemmeException;

	public void endImport(String importId) throws FemmeException;

	public String importCollection(String importId, Collection collection) throws FemmeException;

	public String importInCollection(String importId, DataElement dataElement) throws FemmeException;

	public String insert(Collection collection) throws FemmeException;

	public String insert(DataElement dataElement) throws FemmeException;

	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeException;
	

	public List<Collection> getCollections() throws FemmeException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset) throws FemmeException, FemmeClientException;

	public List<Collection> getCollections(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;

	public <T extends Criterion> List<Collection> findCollections(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException;
	
	public Collection getCollectionById(String id) throws FemmeException;

	public Collection getCollectionByEndpoint(String endpoint) throws FemmeException, FemmeClientException;

	public Collection getCollectionByName(String name) throws FemmeException, FemmeClientException;
	

	public List<DataElement> getDataElements() throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset) throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElements(Integer limit, Integer offset, List<String> includes, List<String> excludes, String xPath) throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElementsInMemoryXPath(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException;
	
	public List<DataElement> getDataElementsInMemoryXPath(Integer limit, Integer offset, List<String> includes, List<String> excludes, String xPath) throws FemmeException, FemmeClientException;

	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath) throws FemmeException, FemmeClientException;

	public <T extends Criterion> List<DataElement> findDataElements(Query<T> query, QueryOptionsMessenger options, String xPath, boolean inMemoryXPath) throws FemmeException, FemmeClientException;
	
	public DataElement getDataElementById(String id) throws FemmeException;

	public DataElement getDataElementById(String id, Set<String> includes, Set<String> excludes) throws FemmeException;

	public DataElement getDataElementById(String id, String xPath) throws FemmeException;

	public DataElement getDataElementById(String id, String xPath, Set<String> includes, Set<String> excludes) throws FemmeException;
	
	public DataElement xPathDataElementWithName(String name, String xPath) throws FemmeException, FemmeClientException;
	
	public DataElement xPathInMemoryDataElementWithName(String name, String xPath) throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElementsByName(String name) throws FemmeException, FemmeClientException;
	

	public List<DataElement> getDataElementsInCollectionById(String collectionId) throws FemmeException, FemmeClientException;

	public List<DataElement> getDataElementsInCollectionById(String collectionId, Integer limit, Integer offset) throws FemmeException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint) throws FemmeException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByEndpoint(String endpoint, Integer limit, Integer offset) throws FemmeException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByName(String name) throws FemmeException, FemmeClientException;
	
	public List<DataElement> getDataElementsInCollectionByName(String name, Integer limit, Integer offset) throws FemmeException, FemmeClientException;
	
}