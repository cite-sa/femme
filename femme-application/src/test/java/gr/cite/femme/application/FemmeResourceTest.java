package gr.cite.femme.application;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.uri.UriComponent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.dto.CollectionList;
import gr.cite.femme.dto.DataElementList;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.query.api.QueryOptionsMessenger;

public class FemmeResourceTest {
	private Client client;
	
	private WebTarget webTarget;
	
	
//	@Before
	public void init() {
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		webTarget = client.target("http://localhost:8081/femme-application-devel/");
	}

//	@Test
	public void query() {
		FemmeResponse<DataElementList> response = webTarget
				.path("dataElements")
				.queryParam("xpath", "//RectifiedGrid[@dimension=2]")
				.request().get(new GenericType<FemmeResponse<DataElementList>>(){});

		System.out.println(response);
	}

//	@Test
	public void insert() throws JsonProcessingException {
//		Collection collection = createDemoCollection();
		
		QueryOptionsMessenger options = new QueryOptionsMessenger();
		options.setLimit(5);
		HashSet<String> exclude = new HashSet<>();
		exclude.add("metadata");
		options.setExclude(exclude);
		
		ObjectMapper mapper = new ObjectMapper();
		String optionsJson = mapper.writeValueAsString(options);

		
		FemmeResponse<CollectionList> response = webTarget
			.path("collections")
			.queryParam("options", UriComponent.encode(optionsJson, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
			.request()
			.get(new GenericType<FemmeResponse<CollectionList>>(){});
		
		System.out.println(response);
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
		Collection collection = Collection.builder().build();
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
