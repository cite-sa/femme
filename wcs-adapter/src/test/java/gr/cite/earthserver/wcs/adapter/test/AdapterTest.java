package gr.cite.earthserver.wcs.adapter.test;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gr.cite.earthserver.wcs.adapter.request.WCSAdapterCoverages;
import gr.cite.earthserver.wcs.adapter.request.WCSAdapterRequest;
import gr.cite.earthserver.wcs.adapter.request.WCSAdapterServers;
import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.utils.Pair;

public class AdapterTest {
	
	//@Test
	public void test() throws JsonProcessingException, FemmeDatastoreException {
//		serverProperties.put("endpoint", "endpoint2");
//		coverageProperties.put("name", "frt00014174_07_if166s_trr3");
//		coverageProperties.put("id", "2");
		
		WCSAdapterServers servers = new WCSAdapterServers();
		WCSAdapterCoverages coverages = new WCSAdapterCoverages();
		
		servers
			.or()
			.attribute(new Pair<String, String>("endpoint", "http://access.planetserver.eu:8080/rasdaman/ows"))
			.attribute(new Pair<String, String>("endpoint", "https://rsg.pml.ac.uk/rasdaman/ows"));
		
		/*coverages
		.or()
		.attribute(new Pair<String, String>("name", "frt00014174_07_if166s_trr3"));*/
		
		WCSAdapterRequest request = new WCSAdapterRequest(servers, coverages);
		
		ObjectMapper mapper = new ObjectMapper();
		
		/*String jsonRequest = mapper.writeValueAsString(request);
		System.out.println(jsonRequest);*/
		
		
		QueryClient query = request.mapToQuery();
		
		String jsonQuery = mapper.writeValueAsString(query);
		System.out.println(jsonQuery);
		
		FemmeClientAPI femmeClient = new FemmeClient();
		List<DataElement> dataElements = femmeClient.findDataElements(query, 5, null, null);
		
		System.out.println(dataElements);
		
	}
}
