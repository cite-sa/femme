package gr.cite.femme.metadata.xpath.mongodb.evaluation.test;

import static org.junit.Assert.assertTrue;

import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.metadata.xpath.MetadataXPath;
import gr.cite.femme.metadata.xpath.elasticsearch.ElasticMetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.grammar.XPathLexer;
import gr.cite.femme.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.metadata.xpath.mongodb.MongoMetadataAndSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.mongodb.MongoMetadataSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoQuery;
import gr.cite.femme.model.Metadatum;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gr.cite.femme.metadata.xpath.parser.visitors.MongoXPathVisitor;

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
	private static final String VALID_EXPRESSION = "/wcs:CoverageDescriptions/wcs:CoverageDescription[gml:id='hrl0000c067_07_if185l_trr3']";


//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
	private static final String INVALID_EXPRESSION = "/server//coverage/@**[local-name()='test']";

	private MetadataXPath xPath;
	private MongoMetadataAndSchemaIndexDatastore xPathDatastore;

	@Before
	public void initXPathClient() throws UnknownHostException {
		/*xPathDatastore = new MongoMetadataAndSchemaIndexDatastore();*/

		xPath = new MetadataXPath(new MongoMetadataSchemaIndexDatastore(), new ElasticMetadataIndexDatastore());
	}

	@After
	public void closeMongoClient() throws IOException {
		xPath.close();
	}

	@Test
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
		String tempXml = "<a>" +
					"<b bAttr=\"test1b\">" +
						"<c cAttr=\"test1c\">" +
							"<d dAttr=\"test1d\">" +
							"</d>" +
						"</c>" +
					"</b>" +
					"<b bAttr=\"test2b\">" +
						"<c cAttr=\"test2c\">" +
							"<d dAttr=\"test2d\">" +
							"</d>" +
						"</c>" +
					"</b>" +
				"</a>";
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

		String json = XmlJsonConverter.xmlToJson(xml);

		List<MaterializedPathsNode> nodes = PathMaterializer.materialize(new ObjectId().toString(), json);
		materializedPaths.insertMany(nodes);
	}*/

//	@Test
	public void xPathTest() {
//		materializedPaths.find()
	}

	//@Test
	public void valid() throws UnknownHostException {

		CharStream stream = new ANTLRInputStream(VALID_EXPRESSION);
		XPathLexer lexer = new XPathLexer(stream);
		XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

		ParseTree tree = parser.xpath();
        //Bson mongoQuery = xPathQuery.query(VALID_EXPRESSION);

		MongoXPathVisitor visitor = new MongoXPathVisitor(new MongoMetadataSchemaIndexDatastore(), new ElasticMetadataIndexDatastore());
		visitor.visit(tree);

		/*System.out.println(visitor.getRegexBuilder().toString());*/
		/*mongoQuery.appendPathRegEx("$'");
		System.out.println("Final query: " + mongoQuery.getPathRegEx().toString());*/

	}

	//@Test
	public void invalid() {

		CharStream stream = new ANTLRInputStream(INVALID_EXPRESSION);
		XPathLexer lexer = new XPathLexer(stream);
		XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

		// suppress errors
		//parser.removeErrorListeners();

		ParseTree tree = parser.xpath();

		assertTrue(parser.getNumberOfSyntaxErrors() > 0);
	}
}
