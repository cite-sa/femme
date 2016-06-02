package gr.cite.femme.application;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.Test;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Metadatum;

public class FemmeResourceTest {
	private Client client;
	
	private WebTarget webTarget;
	
	
	@Before
	public void init() {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target("http://localhost:8081/femme-application/femme/");
	}
	
	@Test
	public void insert() {
		Collection collection = createDemoCollection();
		
		String insertedCollectionId = webTarget
			.path("collections")
			.request()
			.post(Entity.entity(collection, MediaType.APPLICATION_JSON), String.class);
		
		System.out.println(insertedCollectionId);
	}
	
	/*@Test*/
	public void insertMany() {
		List<Collection> collections = new ArrayList<>();
		collections.add(createDemoCollection());
		collections.add(createDemoCollection());
		
		
		List<String> insertedCollectionsIds = webTarget
			.path("collections")
			.request()
			.post(Entity.entity(collections, MediaType.APPLICATION_JSON), List.class);
		
		System.out.println(insertedCollectionsIds);
	}
	
	private DataElement createDemoDataElement(String name, String endpoint) {
		DataElement dataElement = new DataElement();
		if (name != null) {
			dataElement.setName(name);
		} else {
			dataElement.setName("testDataElement" + RandomUtils.nextInt(0, 10));
		}
		if (endpoint != null) {
			dataElement.setEndpoint(endpoint);
		} else {
			dataElement.setEndpoint("http://www.cite-sa/gr/" + RandomUtils.nextInt(0, 10));
		}
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new Metadatum("dc", "<dc><a>test value 1</a></dc>", "xml"));
		metadata.add(new Metadatum("test", "{testJson:{a:1, b:2}}", "json"));
		dataElement.setMetadata(metadata);
		
		List<DataElement> embeddedDataElements = new ArrayList<>();
		DataElement embeddedDataElement1 = new DataElement();
		embeddedDataElement1.setName("embeddedTestDataElement" +  + RandomUtils.nextInt(0, 10));
		embeddedDataElement1.setEndpoint("http://www.cite-sa/gr/" +  + RandomUtils.nextInt(0, 10));
		embeddedDataElements.add(embeddedDataElement1);
		
		DataElement embeddedDataElement2 = new DataElement();
		embeddedDataElement2.setName("embeddedTestDataElement" +  + RandomUtils.nextInt(11, 20));
		embeddedDataElement2.setEndpoint("http://www.cite-sa/gr/" +  + RandomUtils.nextInt(11, 20));
		embeddedDataElements.add(embeddedDataElement2);
		
		dataElement.setDataElements(embeddedDataElements);
		
		return dataElement;
	}
	
	private Collection createDemoCollection() {
		Collection collection = new Collection();
		collection.setName("testCollection" + RandomUtils.nextInt(0, 10));
		collection.setEndpoint("http://www.cite-sa/gr/");
		
		List<Metadatum> metadata = new ArrayList<>();
		Metadatum metadatum = new Metadatum();
		metadatum.setContentType("xml");
		metadatum.setValue("<dc><a>test value</a></dc>");
		metadata.add(metadatum);
		/*metadata.add(new Metadatum("dc", "<dc><a>test value</a></dc>", "xml"));*/
		collection.setMetadata(metadata);
		
		/*List<DataElement> dataElements = new ArrayList<>();
		dataElements.add(createDemoDataElement(null, null));
		dataElements.add(createDemoDataElement(null, null));
		collection.setDataElements(dataElements);*/
		
		return collection;
	}
}
