package gr.cite.femme.engine.metadata.xpath.mongodb.evaluation;

import static org.junit.Assert.assertTrue;

import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.engine.metadata.xpath.MetadataXPath;
//import gr.cite.femme.engine.metadata.xpath.mongodb.MongoMetadataAndSchemaIndexDatastore;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.ElasticMetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.mongodb.MongoMetadataSchemaDatastore;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.UnknownHostException;

public class MongoXPathTest {
	private static final String DB_HOST = "localhost:27017";
	private static final String DB_NAME = "paths-db";

//	private static final String VALID_EXPRESSION = "//server/coverage[@endpoint='http://coverage']";
//	private static final String VALID_EXPRESSION = "//server/coverage";
//	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription[@gml:id='hrl0000c067_07_if185l_trr3']";
//	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:rangeType/swe:DataRecord/swe:field[@name='band_1']";
	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription/@gml:id";
//	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:metadata[gmlcov:Extension/adding_target='MARS']";
//	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription/domainSet/RectifiedGrid/origin/Point[@gml:id='hrl0000c067_07_if185l_trr3-origin']/pos[.='-11827.9751 -526700.332']";





//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
	private static final String INVALID_EXPRESSION = "/server//coverage/@**[local-name()='test']";

	private MetadataXPath xPath;
	//private MongoMetadataAndSchemaIndexDatastore xPathDatastore;

	@Before
	public void initXPathClient() throws UnknownHostException, MetadataIndexException {
		/*xPathDatastore = new MongoMetadataAndSchemaIndexDatastore();*/

		MetadataSchemaIndexDatastore schemaIndexDatastore = null;
		xPath = new MetadataXPath(schemaIndexDatastore, new ElasticMetadataIndexDatastore(schemaIndexDatastore));
	}

	/*@After
	public void closeMongoClient() throws IOException {
		xPath.close();
	}*/

	//@Test
	public void index() throws IOException, MetadataIndexException, HashGenerationException {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");

		String xml = webTarget
				.queryParam("service", "WCS")
				.queryParam("version", "2.0.1")
				.queryParam("request", "DescribeCoverage")
				.queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
				.request().get(String.class);

		Metadatum metadatum = new Metadatum();
		metadatum.setId(new ObjectId().toString());
		metadatum.setElementId(new ObjectId().toString());
		metadatum.setContentType(MediaType.APPLICATION_XML);
		String tempXml = "<employee>"
				+ "<person>"
					+ "<name>name1</name>"
					+ "<friend>"
						+ "<lname>friend11</name>"
					+ "</friend>"
					+ "<friend>"
						+ "<name>friend12</name>"
					+ "</friend>"
				+ "</person>"
				+ "<person>"
					+ "<name>name2</name>"
					+ "<friend>"
						+ "<name>friend21</name>"
					+ "</friend>"
					+ "<friend>"
						+ "<name>friend22</name>"
					+ "</friend>"
				+ "</person>"
			+ "</employee>";
		metadatum.setValue(xml);

		xPath.index(metadatum);
	}

	//@Test
	public void query() throws MetadataIndexException {
		String xPath = "//RectifiedGrid[@dimension=2]";
		this.xPath.xPath(xPath);
	}

	//@Test
	public void xPath() throws MetadataIndexException {
		String xPath = "//RectifiedGrid[@dimension=2]";
		this.xPath.xPath(xPath);
	}

//	@Test
	/*public void materializeTest() throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");

		String xml = webTarget
				.queryParam("service", "WCS")
				.queryParam("version", "2.0.1")
				.queryParam("request", "DescribeCoverage")
				.queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
				.request().get(String.class);

		String json = XmlJsonConverter.xmlToFemmeJson(xml);

		List<MaterializedPathsNode> nodes = PathMaterializer.materialize(new ObjectId().toString(), json);
		materializedPaths.insertMany(nodes);
	}*/

	@Test
	public void valid() throws MetadataIndexException {
		xPath.xPath(VALID_EXPRESSION);
	}

	//@Test
	public void invalid() throws MetadataIndexException {
		xPath.xPath(INVALID_EXPRESSION);
	}
}
