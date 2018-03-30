package gr.cite.earthserver.wcs.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

import gr.cite.earthserver.wcs.core.*;
import gr.cite.earthserver.wcs.utils.WCSParseUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gr.cite.earthserver.wcs.adapter.api.WCSAdapterAPI;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;

public class WCSAdapterTest {
	
	private WCSAdapterAPI wcsAdapter;
	
//	@Before
	public void init() {
		this.wcsAdapter = new WCSAdapter("http://localhost:8081/femme-application");
//		this.wcsAdapter = new WCSAdapter("http://es-devel1.local.cite.gr:8080/femme-application-0.0.1-SNAPSHOT");
	}

//	@Test
	public void retrieve() throws WCSRequestException, ParseException {
		String endpoint = "http://access.planetserver.eu:8080/rasdaman/ows";
		String getCapabilities = new WCSRequestBuilder().endpoint(endpoint).getCapabilities().build().get().getResponse();
		List<String> ids = WCSParseUtils.getCoverageIds(getCapabilities);

		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<Future<String>> futures = new ArrayList<>();
		for (int i = 0; i < ids.size(); i ++) {
			final int inc = i;
			futures.add(executor.submit(() -> {
				String describeCoverage = null;
				String id = null;
				try {
					describeCoverage = new WCSRequestBuilder().endpoint(endpoint).describeCoverage().coverageId(ids.get(inc)).build().get().getResponse();
					id = WCSParseUtils.getCoverageId(describeCoverage);
//					System.out.println();
				} catch (ParseException | WCSRequestException e) {
					e.printStackTrace();
				}
				return inc + ": " + id;
			}));
		}

//		List<String> idAndInc = new ArrayList<>();
		for (Future<String> future: futures) {
			try {
				String result = future.get();
//				idAndInc.add(result);
				System.out.println(result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				executor.shutdown();
			}
		}
//		List<String> sorted = idAndInc.stream().sorted().collect(Collectors.toList());
		/*for (int i = 0; i < ids.size(); i ++) {
			String describeCoverage = new WCSRequestBuilder().endpoint(endpoint).describeCoverage().coverageId(ids.get(i)).execute().get().getResponse();
			System.out.println(i + ": " + WCSParseUtils.getCoverageId(describeCoverage));
		}*/

	}
	
//	@Test
	public String insertServer(String endpoint, String name, WCSResponse server) throws ParseException, FemmeException {
		return null;
	}

//	@Test
	public String insertCoverage(WCSResponse coverage) throws ParseException, FemmeException {
		return null;
	}
	
//	@Test
	public String addCoverage(WCSResponse coverage, String collectionId) throws ParseException, FemmeException {
		return null;
	}
	
//	@Test
	public void getServers() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getServers().size() > 0);
	}
	
//	@Test
	public void getServersLimitOffset()  throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getServers(1, 0).size() == 1);
	}
	
//	@Test
	public void getServersXPath()  throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getServers(1, 0).size() == 1);
	}
	
//	@Test
	public void getServerEndpoint() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getServer("https://rsg.pml.ac.uk/rasdaman/ows") != null);
	}
	
//	@Test
	public void getServerName() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getServer("1412f147-7468-4dcc-bcd4-5858c816b81b") != null);
	}

//	@Test
	public <T extends Criterion> List<Server> findServers(Query<T> query, QueryOptionsMessenger options, String xPath)
			throws FemmeException, FemmeClientException {
				return null;
	}
	
//	@Test
	public void getCoverages() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoverages().size() == 59);
	}
	
//	@Test
	public void getCoveragesLimitOffset() throws FemmeException, FemmeClientException {
		List<Coverage> coverages = this.wcsAdapter.getCoverages(1, 5);
		assertTrue(coverages.size() ==1 && coverages.get(0).getCoverageId().equals("small_no_nulls"));
	}
	
//	@Test
	public List<Coverage> getCoverages(Integer limit, Integer offset, String xPath) throws FemmeException, FemmeClientException {
		return null;
	}
	
//	@Test
	public void getCoverageById() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoverageById("582493bfcd42310e58c7d4b7").getCoverageId().equals("small_no_nulls"));
	}
	
//	@Test
	public <T extends Criterion> List<Coverage> findCoverages(Query<T> query, QueryOptionsMessenger options, String xPath)
			throws FemmeException, FemmeClientException, FemmeClientException {
				return null;
	}
	
//	@Test
	public void getCoverageIds() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoverageIds().size() == 59);
	}
	
//	@Test
	public void getCoverageIdsInServer() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoverageIdsInServer(Arrays.asList("1412f147-7468-4dcc-bcd4-5858c816b81b")).size() == 59);
		assertTrue(this.wcsAdapter.getCoverageIdsInServer(Arrays.asList("https://rsg.pml.ac.uk/rasdaman/ows")).size() == 59);
	}
	
//	@Test
	public void getCoverageIdsInServerLimitOffset() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoverageIdsInServer(Arrays.asList("https://rsg.pml.ac.uk/rasdaman/ows"), 1, null, null).size() == 1);
	}
	
//	@Test
	public void getCoveragesByCoverageId() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoveragesByCoverageId("small_no_nulls").size() == 1);
	}

//	@Test
	public void getCoveragesInServer() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoveragesInServer(Arrays.asList("1412f147-7468-4dcc-bcd4-5858c816b81b")).size() == 59);
		assertTrue(this.wcsAdapter.getCoveragesInServer(Arrays.asList("https://rsg.pml.ac.uk/rasdaman/ows")).size() == 59);
	}
	
//	@Test
	public void getCoveragesInServerLimitOffset() throws FemmeException, FemmeClientException {
		assertTrue(this.wcsAdapter.getCoveragesInServer(Arrays.asList("1412f147-7468-4dcc-bcd4-5858c816b81b"), 5, null, null).size() == 5);
		assertTrue(this.wcsAdapter.getCoveragesInServer(Arrays.asList("https://rsg.pml.ac.uk/rasdaman/ows"), 5, null, null).size() == 5);
	}
	
//	@Test
	public void getCoverageByCoverageIdInServer() throws FemmeException, FemmeClientException {
		assertEquals(this.wcsAdapter.getCoverageByCoverageIdInServer("1412f147-7468-4dcc-bcd4-5858c816b81b", "small_no_nulls").getCoverageId(), "small_no_nulls");
		assertEquals(this.wcsAdapter.getCoverageByCoverageIdInServer("https://rsg.pml.ac.uk/rasdaman/ows", "small_no_nulls").getCoverageId(), "small_no_nulls");
	}
	
	
	
//	@Test
	public void test() throws JsonProcessingException, FemmeException {
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
