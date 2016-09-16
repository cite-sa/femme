package gr.cite.earthserver.wcs.adapter.test;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.earthserver.wcs.adapter.request.WCSAdapterRequest;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.DataElement;

public class AdapterTest {
	
	//@Test
	public void test() throws JsonProcessingException, FemmeDatastoreException {
		Multimap<String, String> serverProperties = ArrayListMultimap.create();
		Multimap<String, String> coverageProperties = ArrayListMultimap.create();
		serverProperties.put("endpoint", "http://access.planetserver.eu:8080/rasdaman/ows");
//		serverProperties.put("endpoint", "endpoint2");
		coverageProperties.put("name", "frt00014174_07_if166s_trr3");
//		coverageProperties.put("id", "2");
		
		QueryClient query = WCSAdapterRequest.request()
				.servers().or(serverProperties)
				/*.coverages().or(coverageProperties)*/
				.get();
		
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(query);
		
		System.out.println(json);
		
		FemmeClientAPI femmeClient = new FemmeClient();
		List<DataElement> dataElements = femmeClient.findDataElements(query, 5, null, null);
		
		System.out.println(dataElements);
		
	}
}
