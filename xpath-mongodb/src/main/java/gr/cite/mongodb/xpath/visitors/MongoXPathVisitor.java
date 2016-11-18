package gr.cite.mongodb.xpath.visitors;

import gr.cite.mongodb.xpath.evaluation.MongoQuery;
import gr.cite.mongodb.xpath.grammar.XPathBaseVisitor;
import gr.cite.mongodb.xpath.grammar.XPathParser.AbsoluteLocationPathNorootContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.AxisSpecifierContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.ExprContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.LocationPathContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.PathExprNoRootContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.RelativeLocationPathContext;
import gr.cite.mongodb.xpath.grammar.XPathParser.StepContext;

public class MongoXPathVisitor extends XPathBaseVisitor<MongoQuery> {
	
	private MongoQuery mongoQuery;
	
	private String scope;
	
	public MongoXPathVisitor(MongoQuery mongoQuery) {
		this.mongoQuery = mongoQuery;
	}
	
	@Override
	public MongoQuery visitExpr(ExprContext ctx) {
		System.out.println("Expr: " + ctx.getText());
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitLocationPath(LocationPathContext ctx) {
		System.out.println("LocationPath: " + ctx.getText());
		for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("LocationPathChild: " + ctx.getChild(i).getText());
		}
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitRelativeLocationPath(RelativeLocationPathContext ctx) {
		System.out.println(ctx.getStart().getText());
		System.out.println("RelativeLocationPath: " + ctx.getText());
		for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("RelativeLocationPathChild: " + ctx.getChild(i).getText());
		}
		if ("/".equals(this.scope)) {
			 this.mongoQuery.append(key, value);
		} else if ("//".equals(this.scope)) {
			
		}
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitAbsoluteLocationPathNoroot(AbsoluteLocationPathNorootContext ctx) {
		System.out.println("AbsoluteLocationPath: " + ctx.getText());
		for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("AbsoluteLocationPathChild: " + ctx.getChild(i).getText());
		}
		if ("/".equals(ctx.getChild(0).getText()) || "//".equals(ctx.getChild(0).getText())) {
			this.scope = ctx.getChild(0).getText();
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
		for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("Step child: " + ctx.getChild(i).getText());
		}
		System.out.println("Step: " + ctx.getText());
		return visitChildren(ctx);
	}
	
	@Override
	public MongoQuery visitAxisSpecifier(AxisSpecifierContext ctx) {
		System.out.println("Axis: " + ctx.getText());
		return visitChildren(ctx);
	}
	
	
}
