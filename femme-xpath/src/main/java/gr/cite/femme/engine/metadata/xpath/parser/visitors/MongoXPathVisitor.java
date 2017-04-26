package gr.cite.femme.engine.metadata.xpath.parser.visitors;

import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Node;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Tree;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathBaseVisitor;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathParser;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;

public class MongoXPathVisitor extends XPathBaseVisitor<Tree<QueryNode>> {

	private MetadataSchemaIndexDatastore metadataSchemaDatastore;
	private QueryNode filterBuilder;
	private Tree<QueryNode> queryTree;
	private List<Node<QueryNode>> currentLevelNodes;
	private Set<String> metadataIndices;
	private boolean predicateMode;

	public MongoXPathVisitor(MetadataSchemaIndexDatastore metadataSchemaDatastore) {
		this.metadataSchemaDatastore = metadataSchemaDatastore;

		this.metadataIndices = new HashSet<>();

		this.queryTree = new Tree<>();
		this.queryTree.getRoot().setData(new QueryNode());
		this.currentLevelNodes = new ArrayList<>();
		this.currentLevelNodes.add(queryTree.getRoot());

		this.filterBuilder = new QueryNode();
	}

	@Override
	public Tree<QueryNode> visitXpath(XPathParser.XpathContext ctx) {
		visitChildren(ctx);
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitAbsoluteLocationPathNoroot(XPathParser.AbsoluteLocationPathNorootContext ctx) {
		String rootPath = ctx.getChild(0).getText();
		if ("/".equals(rootPath)) {
			this.filterBuilder.getNodePath().append("^");
		}
		visit(ctx.relativeLocationPath());
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitRelativeLocationPath(XPathParser.RelativeLocationPathContext ctx) {

		for (int i = 0; i < ctx.getChildCount(); i ++) {
			if ("/".equals(ctx.getChild(i).getText())) {
				if (predicateMode) {
					this.filterBuilder.getFilterPath().append(".");
				} else {
					this.filterBuilder.getNodePath().append("\\.");
				}
			} else if ("//".equals(ctx.getChild(i).getText())) {
				if (predicateMode) {
					this.filterBuilder.getFilterPath().append("\\.*");
				} else {
					this.filterBuilder.getNodePath().append("\\.*");
				}
			} else {
				visit(ctx.getChild(i));

				if (!predicateMode) {
					List<Node<QueryNode>> newLevelNodes = new ArrayList<>();
					this.currentLevelNodes.forEach(node -> {

						/*this.metadataSchemaDatastore.findMetadataIndexPath(node.getData().getNodePath().toString() + this.filterBuilder.getNodePath().toString() + "$").stream()
								.map(MetadataSchema::getSchema).flatMap(Set::stream).map(JSONPath::getPath).distinct().forEach(jsonPath -> {
									QueryNode childNodeData = new QueryNode();
									childNodeData.setNodePath(new StringBuilder(jsonPath));
									childNodeData.setFilterPath(new StringBuilder(this.filterBuilder.getFilterPath()));
									childNodeData.setOperator(this.filterBuilder.getOperator());
									childNodeData.setValue(this.filterBuilder.getValue());

									Node<QueryNode> childNode = new Node<>();
									childNode.setData(childNodeData);
									node.addChild(childNode);
									newLevelNodes.add(childNode);
								});*/


						this.metadataSchemaDatastore.findMetadataIndexPathByRegexAndGroupById(node.getData().getNodePath().toString() + this.filterBuilder.getNodePath().toString() + "$")
								.forEach((path, ids) -> {
									QueryNode childNodeData = new QueryNode();
									childNodeData.setNodePath(new StringBuilder(path));
									childNodeData.setFilterPath(new StringBuilder(this.filterBuilder.getFilterPath()));
									childNodeData.setOperator(this.filterBuilder.getOperator());
									childNodeData.setValue(this.filterBuilder.getValue());

									childNodeData.setMetadataSchemaIds(ids);

									Node<QueryNode> childNode = new Node<>();
									childNode.setData(childNodeData);
									node.addChild(childNode);
									newLevelNodes.add(childNode);

								});
						});


					this.currentLevelNodes = newLevelNodes;
					this.filterBuilder = new QueryNode();
				} else {
					// TODO multiple filter paths (node1[node2//node3='value'])
					System.out.println(this.filterBuilder.getFilterPath().toString());
				}
			}
		}
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitStep(XPathParser.StepContext ctx) {
		visitChildren(ctx);
		return this.queryTree;
	}
	
	@Override
	public Tree<QueryNode> visitAxisSpecifier(XPathParser.AxisSpecifierContext ctx) {
		if (predicateMode) {
			if ("@".equals(ctx.getText())) {
				this.filterBuilder.getFilterPath().append("@.");
			}
		}
		visitChildren(ctx);
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitNodeTest(XPathParser.NodeTestContext ctx) {
		visitChildren(ctx);
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitPredicate(XPathParser.PredicateContext ctx) {
		predicateMode = true;
		visitChildren(ctx);
		predicateMode = false;

		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitEqualityExpr(XPathParser.EqualityExprContext ctx) {
		if (predicateMode) {
			String operator = null;
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if ("=".equals(ctx.getChild(i).getText()) || "!=".equals(ctx.getChild(i).getText())) {
					operator = ctx.getChild(i).getText();
				}
			}
			this.filterBuilder.setOperator(QueryNode.Operator.getOperationEnum(operator));
		}
		visitChildren(ctx);
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitFilterExpr(XPathParser.FilterExprContext ctx) {
		/*filterBuilders.forEach(filter -> filter.append(":").append(ctx.getText()));*/
		this.filterBuilder.setValue(ctx.getText().replace("'", ""));

		visitChildren(ctx);
		return this.queryTree;
	}

	/*@Override
	public Tree<QueryNode> visitPrimaryExpr(XPathParser.PrimaryExprContext ctx) {
		filterBuilders.forEach(filter -> filter.append(ctx.getText()));

		visitChildren(ctx);
		return query;
	}*/

	@Override
	public Tree<QueryNode> visitNameTest(XPathParser.NameTestContext ctx) {
		if ("*".equals(ctx.getText())) {
			if (predicateMode) {
				this.filterBuilder.getFilterPath().append(".+");
			} else {
				this.filterBuilder.getNodePath().append(".+");
			}
		} else if (ctx.getText().contains(":*")) {
			visit(ctx.nCName());
			if (predicateMode) {
				this.filterBuilder.getFilterPath().append(":\\.+");
			} else {
				this.filterBuilder.getNodePath().append(":\\.+");
			}
		} else {
			visitChildren(ctx);
		}
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitQName(XPathParser.QNameContext ctx) {
		for (int i = 0; i < ctx.getChildCount(); i ++) {
			if (":".equals(ctx.getChild(i).getText())) {
				if (predicateMode) {
					this.filterBuilder.getFilterPath().append(":");
				} else {
					this.filterBuilder.getNodePath().append(":");
				}
			} else {
				visit(ctx.getChild(i));
			}
		}
		return this.queryTree;
	}

	@Override
	public Tree<QueryNode> visitNCName(XPathParser.NCNameContext ctx) {
		if (predicateMode) {
			this.filterBuilder.getFilterPath().append(ctx.getText());
		} else {
			this.filterBuilder.getNodePath().append(ctx.getText());
		}
		visitChildren(ctx);
		return this.queryTree;
	}

	/*@Override
	public Tree<QueryNode> visitAbbreviatedStep(XPathParser.AbbreviatedStepContext ctx) {
		if (predicateMode) {
			this.filterBuilder.getFilterPath().append(ctx.getText());
		} else {
			this.filterBuilder.getNodePath().append(ctx.getText());
		}
		visitChildren(ctx);
		return this.queryTree;
	}*/
}
