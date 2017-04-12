package gr.cite.femme.application.test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import gr.cite.earthserver.wcs.adapter.WCSAdapter;
import gr.cite.earthserver.wcs.adapter.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.core.WCSRequest;
import gr.cite.earthserver.wcs.core.WCSRequestBuilder;
import gr.cite.earthserver.wcs.core.WCSRequestException;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.earthserver.wcs.utils.WCSParseUtils;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import org.apache.commons.lang3.RandomUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.uri.UriComponent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.core.dto.CollectionList;
import gr.cite.femme.core.dto.DataElementList;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FemmeApplicationTest {
	private static final String FEMME_URL = "http://localhost:8090/femme-application-devel";

	private WebTarget webTarget;
	private FemmeClientAPI femmeClient;
	private WCSAdapterAPI wcsAdapter;
	
	
	@Before
	public void init() {
		this.webTarget = ClientBuilder.newClient().register(JacksonFeature.class).target(FemmeApplicationTest.FEMME_URL);
		this.femmeClient = new FemmeClient(FemmeApplicationTest.FEMME_URL);
		this.wcsAdapter = new WCSAdapter(FemmeApplicationTest.FEMME_URL);
	}

	@Test
	public void ping() {
		Assert.assertEquals(webTarget.path("ping").request().get(String.class), "pong");
	}

	@Test
	public void importer() throws FemmeDatastoreException, WCSRequestException, ParseException {
		String endpoint = "http://earthserver.ecmwf.int/rasdaman/ows";
		String name = "ECMWF";
		WCSRequestBuilder wcsRequestBuilder = WCSRequest.newBuilder().endpoint(endpoint);

		String importId = this.femmeClient.beginImport(endpoint);

		WCSResponse wcsServerResponse = wcsRequestBuilder.getCapabilities().build().get();
		List<String> coverageIds = WCSParseUtils.getCoverageIds(wcsServerResponse.getResponse());
		Collection collection = WCSFemmeMapper.fromServer(endpoint, name, wcsServerResponse);

		String collectionId = this.femmeClient.importCollection(importId, collection);
		System.out.println("Collection id: " + collectionId);

		//for (int i = 0; i < 2; i ++) {
		for (int i = 1; i < 3; i ++) {
			WCSResponse wcsCoverageResponse = wcsRequestBuilder.describeCoverage().coverageId(coverageIds.get(i)).build().get();
			DataElement dataElement = WCSFemmeMapper.fromCoverage(wcsCoverageResponse);

			if (i == 1) {
				dataElement.setMetadata(WCSFemmeMapper.fromCoverage(wcsRequestBuilder.describeCoverage().coverageId(coverageIds.get(4)).build().get()).getMetadata());
			}

			String dataElementId = this.femmeClient.importInCollection(importId, dataElement);
			System.out.println("DataElement id: " + dataElementId);
		}

		this.femmeClient.endImport(importId);

	}

	//@Test
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
		/*collections.add(createDemoCollection());
		collections.add(createDemoCollection());*/
		
		
		List<String> insertedCollectionsIds = webTarget.path("collections").request()
			.post(Entity.entity(collections, MediaType.APPLICATION_JSON), new GenericType<List<String>>(){});
		
		System.out.println(insertedCollectionsIds);
	}

	private static Collection createTestCollection(String endpoint) {
		Collection collection = new Collection();

		collection.setName("TestCollection");
		collection.setEndpoint(endpoint);
		
		List<Metadatum> metadata = new ArrayList<>();
		Metadatum metadatum = new Metadatum();
		metadatum.setContentType(MediaType.TEXT_XML);
		metadatum.setValue("<dc><a>test value</a></dc>");
		metadata.add(metadatum);

		collection.setMetadata(metadata);
		
		return collection;
	}

	private static DataElement createTestDataElement(String name, String endpoint) {
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
}
