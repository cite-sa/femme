package gr.cite.earthserver.wcs.adapter.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.earthserver.wcs.adapter.WCSAdapter;
import gr.cite.earthserver.wcs.adapter.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.adapter.request.WCSAdapterCoverages;
import gr.cite.earthserver.wcs.adapter.request.WCSAdapterRequest;
import gr.cite.earthserver.wcs.adapter.request.WCSAdapterServers;
import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.utils.Pair;

public class AdapterTest {
	
	private WCSAdapterAPI wcsAdapter;
	
	@Before
	public void init() {
		this.wcsAdapter = new WCSAdapter("http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT");
	}
	
	
	@Test
	public void test() throws JsonProcessingException, FemmeDatastoreException {
//		serverProperties.put("endpoint", "endpoint2");
//		coverageProperties.put("name", "frt00014174_07_if166s_trr3");
//		coverageProperties.put("id", "2");
		
		/*WCSAdapterServers servers = new WCSAdapterServers();
		WCSAdapterCoverages coverages = new WCSAdapterCoverages();
		
		servers
			.or()
			.attribute(new Pair<String, String>("endpoint", "http://access.planetserver.eu:8080/rasdaman/ows"))
			.attribute(new Pair<String, String>("endpoint", "https://rsg.pml.ac.uk/rasdaman/ows"));
		
		coverages
		.or()
		.attribute(new Pair<String, String>("name", "frt00014174_07_if166s_trr3"));
		
		WCSAdapterRequest request = new WCSAdapterRequest(servers, coverages);
		
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonRequest = mapper.writeValueAsString(request);
		System.out.println(jsonRequest);
		
		
		QueryClient query = request.mapToQuery();
		
		String jsonQuery = mapper.writeValueAsString(query);
		System.out.println(jsonQuery);*/
		
		
		
		/*WCSAdapter adapter = new WCSAdapter("http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT");
		List<Coverage> coverages = adapter.getCoverages(5, null);
		
		System.out.println(coverages);
		
		FemmeClientAPI femmeClient = new FemmeClient("http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT");
		List<DataElement> dataElements = femmeClient.findDataElements(null, 5, null, null);
		
		System.out.println(dataElements);*/
		
		/*List<Coverage> coverages = this.wcsAdapter.getCoveragesByCoverageId("frt0000cc22_07_if165l_trr3");*/
		/*System.out.println(coverages);*/
		
	}
}
