package gr.cite.femme.metadata.xpath.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import gr.cite.femme.model.Metadatum;
import org.bson.types.ObjectId;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class MetadataXPathIndexApplicationTest {


    //	private static final String VALID_EXPRESSION = "//server/coverage[@endpoint='http://coverage']";
    private static final String VALID_EXPRESSION = "//server/coverage";
    //	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
    private static final String INVALID_EXPRESSION = "/server//coverage/@**[local-name()='test']";

    private Client client;
    private WebTarget webTarget;

    @Before
    public void init() {
        client = ClientBuilder.newClient().register(JacksonFeature.class);
        webTarget = client.target("http://localhost:8082/femme-metadata-xpath-application");
    }

//    @Test
    public void index() {
        Client wcsClient = ClientBuilder.newClient();
        WebTarget wcsWebTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");
        String xml = wcsWebTarget
                .queryParam("service", "WCS")
                .queryParam("version", "2.0.1")
                .queryParam("request", "DescribeCoverage")
                .queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
                .request().get(String.class);
        xml = xml.replaceAll(">\\s+<", "><").trim();

        Metadatum metadatum = new Metadatum();
        metadatum.setId(new ObjectId().toString());
        metadatum.setContentType(MediaType.APPLICATION_XML);
        metadatum.setValue(xml);

        webTarget.path("metadata").request().post(Entity.entity(metadatum, MediaType.APPLICATION_JSON));
    }

    @Test
    public void xPath() {
        String xPath = "//wcs:CoverageId";

        Response response = webTarget
                .path("metadata").queryParam("xPath", xPath)
                .request().get(Response.class);
        List<MaterializedPathsNode> nodes = response.readEntity(new GenericType<List<MaterializedPathsNode>>(){});
        System.out.println(nodes);
    }
}
