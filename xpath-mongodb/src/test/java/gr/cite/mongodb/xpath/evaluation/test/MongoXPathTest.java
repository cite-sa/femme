package gr.cite.mongodb.xpath.evaluation.test;

import static org.junit.Assert.assertTrue;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import gr.cite.mongodb.xpath.evaluation.MongoQuery;
import gr.cite.mongodb.xpath.grammar.XPathLexer;
import gr.cite.mongodb.xpath.grammar.XPathParser;
import gr.cite.mongodb.xpath.visitors.MongoXPathVisitor;

public class MongoXPathTest {
	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='http://coverage']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
//	private static final String VALID_EXPRESSION = "/server//coverage[@endpoint='test']/@*[local-name()='test']";
	private static final String INVALID_EXPRESSION = "/server//coverage/@**[local-name()='test']";;

	@Test
	public void valid() {

		CharStream stream = new ANTLRInputStream(VALID_EXPRESSION);
		XPathLexer lexer = new XPathLexer(stream);
		XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

		ParseTree tree = parser.xpath();

//		assertTrue(parser.getNumberOfSyntaxErrors() == 0);
		
//		XWCPSEvalVisitor visitor = new XWCPSEvalVisitor(builder, exmmsQuery);
//		Query result = visitor.visit(tree);
//		return result;
		
		MongoXPathVisitor visitor = new MongoXPathVisitor(new MongoQuery());
		MongoQuery result = visitor.visit(tree);
		System.out.println(result);
		
		
		
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
