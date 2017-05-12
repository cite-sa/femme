package gr.cite.commons.utils.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import net.sf.saxon.lib.NamespaceConstant;

/**
 * xpath v2
 * @author ikavvouras
 *
 */
public class XPathEvaluator {
	
	private static final Logger logger = LoggerFactory.getLogger(XPathEvaluator.class);
	private boolean setNamespaceContext;
	
	private boolean topLevelOnly;
	
	private Node node;
	
	private XPath xpath;
	
	private Map<String, String> namespaces;

	
	public XPathEvaluator(Node node) throws XPathFactoryConfigurationException {
		this(node, true, false);
	}

	public XPathEvaluator(Node node, boolean setNamespaceContext) throws XPathFactoryConfigurationException {
		this(node, setNamespaceContext, true);
	}
	
	public XPathEvaluator(Node node, boolean setNamespaceContext, boolean topLevelOnly) throws XPathFactoryConfigurationException {
		
		this.node = node;
		
		String objectModel = NamespaceConstant.OBJECT_MODEL_SAXON;
		System.setProperty("javax.xml.xpath.XPathFactory:" + objectModel, "net.sf.saxon.xpath.XPathFactoryImpl");
		
		XPathFactory factory = XPathFactory.newInstance(objectModel);
		xpath = factory.newXPath();
		
		if (setNamespaceContext) {
			xpath.setNamespaceContext(new UniversalNamespaceCache(node, topLevelOnly));
		}
		
	}
	
	public void setNamespaceContextFromRoot() {
		xpath.setNamespaceContext(new UniversalNamespaceCache(node, true));
	}
	
	public void setNamespaceContextFromDocument() {
		xpath.setNamespaceContext(new UniversalNamespaceCache(node, false));
	}

	public void setNamespaceContext(Map<String, String> namespaces) {
		this.namespaces = namespaces;
		xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix == null) {
					throw new NullPointerException("XPath prefix is null.");
				} else if ("xml".equals(prefix)) {
					return XMLConstants.XML_NS_URI;
				} else {
					String namespaceValue = XPathEvaluator.this.namespaces.get(prefix);
					if (namespaceValue != null) {
						return namespaceValue;
					}
				}
				return XMLConstants.NULL_NS_URI;
			}
			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}
			public Iterator getPrefixes(String uri) {
				throw new UnsupportedOperationException();
			}
		});
	}

	public List<String> evaluate(String expression) throws XPathEvaluationException {
		List<String> list = new ArrayList<String>();
		try {
			NodeList nodeList = (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i).getNodeType() == 2 || nodeList.item(i).getNodeType() == 3) {
					list.add(nodeList.item(i).getTextContent());
				} else
					list.add(XMLConverter.nodeToString(nodeList.item(i)));
			}
		} catch (XPathExpressionException e) {
			try {
				Double number = (Double) xpath.evaluate(expression, node, XPathConstants.NUMBER);
				list.add(String.valueOf(number.intValue()));
			} catch (XPathExpressionException e1) {
				logger.error(e.getMessage(), e);
				throw new XPathEvaluationException("Error during XPath evalution", e);
			}
		} catch (XMLConversionException e) {
			logger.error(e.getMessage(), e);
			throw new XPathEvaluationException("Error during XPath evalution", e);
		}
		
		return list.stream().map(String::trim).filter(input -> input.length() > 0).collect(Collectors.toList());
	}

	public List<Node> evaluateToNode(String expression) throws XPathEvaluationException {
		List<Node> list = new ArrayList<Node>();
		try {
			NodeList nodeList = (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i).getNodeType() == 2 || nodeList.item(i).getNodeType() == 3) {
					list.add(nodeList.item(i));
				} else
					list.add(nodeList.item(i));
			}
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage(), e);
			throw new XPathEvaluationException("Error during XPath evalution", e);
		}

		return list.stream().filter(input -> {
			if (input.getNodeType() == 2 || input.getNodeType() == 3) {
				return input.getTextContent().length() > 0;
			} else {
				try {
					return XMLConverter.nodeToString(node).length() > 0;
				} catch (XMLConversionException e) {
					logger.error(e.getMessage(), e);
					return false;
				}
				
			}
		}).collect(Collectors.toList());
	}

	
}
