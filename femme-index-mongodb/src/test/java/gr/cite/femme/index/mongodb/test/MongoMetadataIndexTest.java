package gr.cite.femme.index.mongodb.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import gr.cite.femme.index.mongodb.MongoMetadataIndex;
import gr.cite.femme.core.model.Metadatum;
import org.bson.types.ObjectId;

public class MongoMetadataIndexTest {
	
	private MongoMetadataIndex index;

//	@Before
	public void init() {
		index = new MongoMetadataIndex("localhost:27017", "femme-index-db");
	}
	
//	@Test
	public void indexMetadatum() throws XMLStreamException {
		Client client = ClientBuilder.newClient();
		 WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");
		 
		 /*String xml = webTarget
				 .queryParam("service", "WCS")
				 .queryParam("version", "2.0.1")
				 .queryParam("request", "DescribeCoverage")
				 .queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
				 .request().get(String.class);*/
		 
		 String xml = "{" +
				"\"server\": {" +
					"\"endpoint\": \"http://server\"" +
					"\"coverages\": {" +
						"\"coverage\": {" +
							"\"@\": {" +
								"\"endpoint\": \"http://coverage\"" +
							"}" +
						"}" +
					"}" +
				"}" +
			"}";
		 
		 Metadatum metadatum = new Metadatum();
		 metadatum.setElementId(new ObjectId().toString());
		 metadatum.setContentType(MediaType.APPLICATION_JSON);
		 metadatum.setName("DescribeCoverage");
		 metadatum.setValue(xml);
		 
		 index.index(metadatum);
		 
	}
}
