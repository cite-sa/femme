package gr.cite.femme.client.test.query;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.query.CriterionClient;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.core.model.Collection;

public class FemmeClientTest {
	
	private FemmeClient client;
	
//	@Before
	public void initClient() {
		client = new FemmeClient("http://localhost:8081/femme-application");
	}
	
//	@Test
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
		
	}

//	@Test	
	public void getCollectionById() throws FemmeDatastoreException {
		Collection collection = client.getCollectionById("582493bdcd42310e58c7d49d");
	}
	
	
	
}
