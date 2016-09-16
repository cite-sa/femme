package gr.cite.femme.client.test.query;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.mongodb.CriterionMongo;

public class FemmeClientTest {
	
	private FemmeClient client;
	
	@Before
	public void initClient() {
		client = new FemmeClient();
	}
	
	@Test
	public void testCriteriaSerialization() throws IOException {
		CriterionClient criteria = CriterionBuilderClient.root()
				.or(Arrays.asList(
					CriterionBuilderClient.root().gt("id", 0).end(),
					CriterionBuilderClient.root().lt("endpoint", 9).and(
							Arrays.asList(CriterionBuilderClient.root().eq("name", "name1").end())
							).end()
		)).end();
		
		ObjectMapper mapper = new ObjectMapper();

		String json = mapper.writeValueAsString(criteria);
		System.out.println(json);
		
		CriterionMongo criterion = mapper.readValue(json, CriterionMongo.class);
		
		System.out.println(criterion);
	}
	
//	@Test
	public void testDatastore() throws FemmeDatastoreException {
		/*DataElement dataElement = new DataElement();
		dataElement.setName("testName");
		dataElement.setEndpoint("testEndpoint");
		
		String id = client.insert(dataElement);
		
		List<DataElement> stored = client.getDataElementByEndpoint(dataElement.getEndpoint());
		for (DataElement element: stored) {
			System.out.println(element.toString());
		}*/
		
		QueryClient query = new QueryClient();
		query.addCriterion(CriterionBuilderClient.root().eq("endpoint", "test").end());
		
//		client.findDataElements(query, null, null);
	}
	
	//@Test
	public void queryDatastore() throws FemmeDatastoreException {
		Collection collection = client.getCollectionByEndpoint("http://access.planetserver.eu:8080/rasdaman/ows");
		System.out.println(collection.getEndpoint());
		
		QueryClient queryForCollection = new QueryClient();
		queryForCollection.addCriterion(CriterionBuilderClient.root().eq("endpoint", "http://access.planetserver.eu:8080/rasdaman/ows").end());
		List<Collection> collections = client.findCollections(queryForCollection, null, null, null);
		for (Collection collectionFromList: collections) {
			System.out.println("Collection from list: " + collectionFromList.getEndpoint());
		}
		
		List<DataElement> dataElements = client.getDataElements(10, null);
		for (DataElement dataElement: dataElements) {
			System.out.println("DataElement from list: " + dataElement.getEndpoint());
		}
		
	}
	
	
	
}
