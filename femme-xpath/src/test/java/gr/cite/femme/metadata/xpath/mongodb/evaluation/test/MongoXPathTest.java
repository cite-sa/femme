package gr.cite.femme.metadata.xpath.mongodb.evaluation.test;

import static org.junit.Assert.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import gr.cite.femme.metadata.xpath.core.MetadataXPath;
import gr.cite.femme.metadata.xpath.grammar.XPathLexer;
import gr.cite.femme.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.metadata.xpath.mongodb.codecs.MaterializedPathsNodeCodecProvider;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoQuery;
import gr.cite.femme.metadata.xpath.transformation.PathMaterializer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import gr.cite.femme.metadata.xpath.parser.visitors.MongoXPathVisitor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.List;

public class MongoXPathTest {
	private static final String DB_HOST = "localhost:27017";
	private static final String DB_NAME = "paths-db";

//	private static final String VALID_EXPRESSION = "//server/coverage[@endpoint='http://coverage']";
//	private static final String VALID_EXPRESSION = "//server/coverage";
	private static final String VALID_EXPRESSION = "//RectifiedGrid[@dimension=2]";

//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
	private static final String INVALID_EXPRESSION = "/server//coverage/@**[local-name()='test']";

	private MetadataXPath xPathDatastore;

//	@Before
	public void initMongoClient() {
		xPathDatastore = new MetadataXPath("localhost:27017", "materialized-paths-db", "paths");
	}

//	@Test
	public void xPath() {
		String xPath = "//RectifiedGrid[@dimension=2]";
		xPathDatastore.xPath(xPath);
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

//	@Test
	public void valid() {

		CharStream stream = new ANTLRInputStream(VALID_EXPRESSION);
		XPathLexer lexer = new XPathLexer(stream);
		XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

		ParseTree tree = parser.xpath();

		/*MongoQuery mongoQuery = new MongoQuery();
		MongoXPathVisitor visitor = new MongoXPathVisitor(mongoQuery);
		visitor.visit(tree);
		mongoQuery.appendPathRegEx("$'");
		System.out.println("Final query: " + mongoQuery.getPathRegEx().toString());*/
		
		
		
	}

//	@Test
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
