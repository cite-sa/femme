package gr.cite.mongodb.xpath.visitors;

import gr.cite.mongodb.xpath.evaluation.MongoQuery;
import gr.cite.mongodb.xpath.grammar.XPathBaseVisitor;
import gr.cite.mongodb.xpath.grammar.XPathParser.LocationPathContext;

public class MongoXPathVisitor extends XPathBaseVisitor<MongoQuery> {
	@Override
	public MongoQuery visitLocationPath(LocationPathContext ctx) {
		System.out.println(ctx);
		return visitChildren(ctx);
	}
}
