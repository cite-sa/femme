package gr.cite.femme.fulltext.client;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FulltextClientTest {
	private FulltextIndexClientAPI client;

	@Before
	public void init() {
		this.client = new FulltextIndexClient("http://localhost:8081/fulltext-application-devel");
	}

	@Test
	public void testInsert() {
		//FulltextDocument doc = new FulltextDocument();
		String elementId = UUID.randomUUID().toString();
		String metadatumId = UUID.randomUUID().toString();
		Map<String, Object> fields = new HashMap<>();
		fields.put("name", "testName");

		this.client.insert(elementId, metadatumId, fields);
	}
}
