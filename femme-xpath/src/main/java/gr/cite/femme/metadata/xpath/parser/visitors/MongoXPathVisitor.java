package gr.cite.femme.metadata.xpath.parser.visitors;

import gr.cite.femme.metadata.xpath.grammar.XPathBaseVisitor;
import gr.cite.femme.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.AbsoluteLocationPathNorootContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.AxisSpecifierContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.ExprContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.FilterExprContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.LocationPathContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.NameTestContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.NodeTestContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.PathExprNoRootContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.PredicateContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.RelativeLocationPathContext;
import gr.cite.femme.metadata.xpath.grammar.XPathParser.StepContext;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoQuery;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MongoXPathVisitor extends XPathBaseVisitor<MongoQuery> {
	
	private MongoQuery mongoQuery;

	private Queue<String> slashQueue = new LinkedList<>();
	
	private List<Document> queryAndList = new ArrayList<>();

	private String tempKey;

	public String axisSpecifier;

	/*public MongoXPathVisitor(MongoQuery mongoQuery) {
		this.mongoQuery = mongoQuery;
	}*/

	public MongoXPathVisitor(List<Document> queryAndList) {
		this.queryAndList = queryAndList;
	}

	public List<Document> getQueryAndList() {
		return queryAndList;
	}

	public void setQueryAndList(List<Document> queryAndList) {
		this.queryAndList = queryAndList;
	}

	@Override
	public MongoQuery visitExpr(ExprContext ctx) {
		System.out.println("Expr: " + ctx.getText());
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitLocationPath(LocationPathContext ctx) {
		System.out.println("LocationPath: " + ctx.getText());
		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("LocationPathChild: " + ctx.getChild(i).getText());
		}*/
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitAbsoluteLocationPathNoroot(AbsoluteLocationPathNorootContext ctx) {
		System.out.println("AbsoluteLocationPathNoRoot: " + ctx.getText());
		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("AbsoluteLocationPathChild: " + ctx.getChild(i).getText());
		}*/
		/*if ("/".equals(ctx.getStart().getText())) {
			mongoQuery.appendPathRegEx("^");
		} else if ("//".equals(ctx.getStart().getText())) {
			mongoQuery.appendPathRegEx("");
		}*/
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitRelativeLocationPath(RelativeLocationPathContext ctx) {
		/*System.out.println(ctx.getStart().getText());*/
		System.out.println("RelativeLocationPath: " + ctx.getText());

		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("RelativeLocationPathChild: " + ctx.getChild(i).getText());
		}*/

		for (int i = 0; i < ctx.getChildCount(); i ++) {
			if ("/".equals(ctx.getChild(i).getText())) {
				slashQueue.add(ctx.getChild(i).getText());
			} else if ("//".equals(ctx.getChild(i).getText())) {
				slashQueue.add("*");
			}
		}
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitPathExprNoRoot(PathExprNoRootContext ctx) {
		System.out.println("PathExprNoRoot: " + ctx.getText());
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitStep(StepContext ctx) {
		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("Step child: " + ctx.getChild(i).getText());
		}*/
		System.out.println("Step: " + ctx.getText());

		/*String separator = slashQueue.size() > 0 ? slashQueue.remove() : "";*/
		/*mongoQuery.appendPathRegEx(ctx.getText() + separator);*/

		/*queryAndList.add(new Document().append("path", ));*/

		/*System.out.println("Mongo query: " + mongoQuery.getPathRegEx().toString());*/
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitAxisSpecifier(AxisSpecifierContext ctx) {
		System.out.println("Axis: " + ctx.getText());
		axisSpecifier = ctx.getText();
		return visitChildren(ctx);
	}


	@Override
	public MongoQuery visitNodeTest(NodeTestContext ctx) {
		System.out.println("Node test: " + ctx.getText());
		/*mongoQuery.appendPathRegEx(ctx.getText());*/
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitNameTest(NameTestContext ctx) {
		System.out.println("Node test: " + ctx.getText());
		if ("".equals(axisSpecifier)) {
			String separator = slashQueue.size() > 0 ? slashQueue.remove() : "";
			queryAndList.add(new Document().append("path", new Document().append("$regex", separator + ctx.getText())));
		} else if ("@".equals(axisSpecifier)) {
			tempKey = axisSpecifier + "." + ctx.getText();
		}
		/*mongoQuery.appendPathRegEx(ctx.getText());*/
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitPredicate(PredicateContext ctx) {
		/*ctx.get*/
		System.out.println("Predicate: " + ctx.getText());
		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("Predicate child: " + ctx.getChild(i).getText());
		}*/
		return visitChildren(ctx);
	}

	@Override
	public MongoQuery visitFilterExpr(FilterExprContext ctx) {
		queryAndList.add(new Document().append(tempKey, ctx.getText()));

		return visitChildren(ctx);
	}
	
	
}
