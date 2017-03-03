package gr.cite.femme.metadata.xpath.parser.visitors;

import gr.cite.femme.metadata.xpath.core.MetadataIndexQuery;
import gr.cite.femme.metadata.xpath.datastores.MetadataIndexDatastore;
import gr.cite.femme.metadata.xpath.datastores.MetadataSchemaIndexDatastore;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.QueryTree;
import gr.cite.femme.metadata.xpath.grammar.XPathBaseVisitor;
import gr.cite.femme.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.metadata.xpath.mongodb.evaluation.MongoQuery;

import java.util.*;
import java.util.stream.Collectors;

public class MongoXPathVisitor extends XPathBaseVisitor<MetadataIndexQuery> {
	
	private MongoQuery mongoQuery;

	private Queue<String> slashQueue = new LinkedList<>();
	
	/*private List<Document> queryAndList = new ArrayList<>();*/

	private String tempKey;

	public String axisSpecifier;


	private MetadataSchemaIndexDatastore metadataSchemaDatastore;
	private MetadataIndexDatastore metadataIndexDatastore;
	private MetadataIndexQuery query = new MetadataIndexQuery();

	private boolean predicateMode;

	private List<StringBuilder> regexBuilders;
	private List<StringBuilder> filterBuilders;
	private List<String> paths;
	private List<List<Map<String, Boolean>>> filters;

	//private List<StringBuilder> filter;
	private String filterOperator;

	private QueryTree<QueryNode> queryTree;
	private int currentTreeLevel = 0;



	public MongoXPathVisitor() {
		this.regexBuilders = new ArrayList<>();
		this.regexBuilders.add(new StringBuilder());
		/*this.regexBuilder = new StringBuilder();*/
		this.filters = new ArrayList<>();

		this.queryTree = new QueryTree<>();
	}

	public MongoXPathVisitor(MetadataSchemaIndexDatastore metadataSchemaDatastore, MetadataIndexDatastore metadataIndexDatastore) {
		this.metadataSchemaDatastore = metadataSchemaDatastore;
		this.metadataIndexDatastore = metadataIndexDatastore;

		this.regexBuilders = new ArrayList<>();
		this.regexBuilders.add(new StringBuilder());
		/*this.regexBuilder = new StringBuilder();*/
		this.filters = new ArrayList<>();

		this.queryTree = new QueryTree<>();
	}

	/*public MongoXPathVisitor(MongoQuery mongoQuery) {
		this.mongoQuery = mongoQuery;
	}*/

	/*public MongoXPathVisitor(List<Document> queryAndList) {
		this.queryAndList = queryAndList;
	}

	public List<Document> getQueryAndList() {
		return queryAndList;
	}*/
/*
	public void setQueryAndList(List<Document> queryAndList) {
		this.queryAndList = queryAndList;
	}*/




	@Override
	public MetadataIndexQuery visitAbsoluteLocationPathNoroot(XPathParser.AbsoluteLocationPathNorootContext ctx) {
		String rootPath = ctx.getChild(0).getText();
		if ("/".equals(rootPath)) {
			regexBuilders.forEach(regExBuilder -> regExBuilder.append("^"));
		}
		visit(ctx.relativeLocationPath());
		return query;
	}

	@Override
	public MetadataIndexQuery visitRelativeLocationPath(XPathParser.RelativeLocationPathContext ctx) {

		for (int i = 0; i < ctx.getChildCount(); i ++) {
			if ("/".equals(ctx.getChild(i).getText())) {
				regexBuilders.forEach(regExBuilder -> regExBuilder.append("\\."));
			} else if ("//".equals(ctx.getChild(i).getText())) {
				regexBuilders.forEach(regExBuilder -> regExBuilder.append("\\.*"));
			} else {
				visit(ctx.getChild(i));
				if (filterBuilders.size() > 0) {
					/*queryTree.addLevel();*/
				}
			}

		}
		return query;
	}

	@Override
	public MetadataIndexQuery visitStep(XPathParser.StepContext ctx) {

		/*String separator = slashQueue.size() > 0 ? slashQueue.remove() : "";*/
		/*mongoQuery.appendPathRegEx(ctx.getText() + separator);*/
		/*queryAndList.add(new Document().append("path", ));*/

		visitChildren(ctx);
		return query;
	}
	
	@Override
	public MetadataIndexQuery visitAxisSpecifier(XPathParser.AxisSpecifierContext ctx) {
		/*axisSpecifier = ctx.getText();*/
		if (predicateMode) {
			filterBuilders.forEach(filterBuilder -> filterBuilder.append("\\.@\\."));
		}
		visitChildren(ctx);
		return query;
	}

	@Override
	public MetadataIndexQuery visitNodeTest(XPathParser.NodeTestContext ctx) {
		/*mongoQuery.appendPathRegEx(ctx.getText());*/
		visitChildren(ctx);
		return query;
	}

	@Override
	public MetadataIndexQuery visitPredicate(XPathParser.PredicateContext ctx) {
		predicateMode = true;
		//filterBuilders = new ArrayList<>(regexBuilders);


		/*filter = regexBuilders.stream().map(regexBuilder ->
				metadataSchemaDatastore.findMetadataIndexPath(regexBuilder.toString()).stream()
					.map(metadataSchema ->
							metadataSchema.getSchema().stream().map(path -> {
								Map<String, Boolean> pathBooleanExpr = new HashMap<>();
								pathBooleanExpr.put(path, true);
								return pathBooleanExpr;
							}).collect(Collectors.toList())
					).flatMap(List::stream).collect(Collectors.toList())
				).flatMap(List::stream).collect(Collectors.toList());*/


		visitChildren(ctx);
		predicateMode = false;

		System.out.println(filterBuilders);

		return query;
	}

	@Override
	public MetadataIndexQuery visitEqualityExpr(XPathParser.EqualityExprContext ctx) {
		/*for (int i = 0; i < ctx.getChildCount(); i++) {
			System.out.println("Predicate child: " + ctx.getChild(i).getText());
		}*/

		if (predicateMode) {
			filterBuilders = regexBuilders.stream().map(regexBuilder ->
					metadataSchemaDatastore.findMetadataIndexPath(regexBuilder.toString() + "$")
							.stream().map(metadataSchema ->
							metadataSchema.getSchema().stream().map(path -> new StringBuilder(path.getPath())/*{
											Map<StringBuilder, Boolean> pathBooleanExpr = new HashMap<>();
											pathBooleanExpr.put(new StringBuilder(path), "=".equals(ctx.getChild(childIndex).getText()));
											return pathBooleanExpr;
										}*/).collect(Collectors.toList())
					).flatMap(List::stream).collect(Collectors.toList())
			).flatMap(List::stream).collect(Collectors.toList());
			/*if ("=".equals(ctx.getChild(i).getText()) || "!=".equals(ctx.getChild(i).getText())) {
				System.out.println("ATTRIBUTE EQUALITY");
			}*/
		}

		/*for (int i = 0; i < ctx.getChildCount(); i ++) {*/
			/*if (predicateMode) {
				final int childIndex = i;
				filterBuilders = regexBuilders.stream().map(regexBuilder ->
						metadataSchemaDatastore.findMetadataIndexPath(regexBuilder.toString() + "$")
						.stream().map(metadataSchema ->
										metadataSchema.getSchema().stream().map(path -> new StringBuilder(path)*//*{
											Map<StringBuilder, Boolean> pathBooleanExpr = new HashMap<>();
											pathBooleanExpr.put(new StringBuilder(path), "=".equals(ctx.getChild(childIndex).getText()));
											return pathBooleanExpr;
										}*//*).collect(Collectors.toList())
								).flatMap(List::stream).collect(Collectors.toList())
				).flatMap(List::stream).collect(Collectors.toList());
				if ("=".equals(ctx.getChild(i).getText()) || "!=".equals(ctx.getChild(i).getText())) {
					System.out.println("ATTRIBUTE EQUALITY");
				}
			}*/
			/*visit(ctx.getChild(i));
		}*/
			visitChildren(ctx);
		return query;
	}

	@Override
	public MetadataIndexQuery visitFilterExpr(XPathParser.FilterExprContext ctx) {
		filterBuilders.forEach(filter -> filter.append(":").append(ctx.getText()));

		visitChildren(ctx);
		return query;
	}

	/*@Override
	public MetadataIndexQuery visitPrimaryExpr(XPathParser.PrimaryExprContext ctx) {
		filterBuilders.forEach(filter -> filter.append(ctx.getText()));

		visitChildren(ctx);
		return query;
	}*/

	@Override
	public MetadataIndexQuery visitNameTest(XPathParser.NameTestContext ctx) {
		/*if ("".equals(axisSpecifier)) {
			String separator = slashQueue.size() > 0 ? slashQueue.remove() : "";
			queryAndList.add(new Document().append("path", new Document().append("$regex", separator + ctx.getText())));
		} else if ("@".equals(axisSpecifier)) {
			tempKey = axisSpecifier + "." + ctx.getText();
		}*/
		/*mongoQuery.appendPathRegEx(ctx.getText());*/
		if ("*".equals(ctx.getText())) {
			/*regexBuilders.forEach(regexBuilder -> regexBuilder.append());*/
			if (predicateMode) {
				appendToStringBuilders(filterBuilders, ".+");
			} else {
				appendToStringBuilders(regexBuilders, ".+");
			}
		} else if (ctx.getText().contains(":*")) {
			visit(ctx.nCName());
			/*regexBuilders.forEach(regexBuilder -> regexBuilder.append(":\\.+"));*/
			if (predicateMode) {
				appendToStringBuilders(filterBuilders, ":\\.+");
			} else {
				appendToStringBuilders(regexBuilders, ":\\.+");
			}
		} else {
			visitChildren(ctx);
		}
		return query;
	}

	@Override
	public MetadataIndexQuery visitQName(XPathParser.QNameContext ctx) {
		for (int i = 0; i < ctx.getChildCount(); i ++) {
			if (":".equals(ctx.getChild(i).getText())) {
				if (predicateMode) {
					appendToStringBuilders(filterBuilders, ":");
				} else {
					appendToStringBuilders(regexBuilders, ":");
				}
			} else {
				visit(ctx.getChild(i));
			}
		}
		return query;
	}

	@Override
	public MetadataIndexQuery visitNCName(XPathParser.NCNameContext ctx) {
		/*regexBuilder.append(ctx.getChild(0).getText());*/
		/*regexBuilder.append(ctx.getText());*/

		/*regexBuilders.forEach(regexBuilder -> regexBuilder.append(ctx.getText()));*/
		if (predicateMode) {
			appendToStringBuilders(filterBuilders, ctx.getText());
		} else {
			appendToStringBuilders(regexBuilders, ctx.getText());
		}
		visitChildren(ctx);
		return query;
	}

	private void appendToStringBuilders(List<StringBuilder> builders, String symbol) {
		builders.forEach(builder -> builder.append(symbol));
	}
	
}
