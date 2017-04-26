package gr.cite.commons.utils.xml;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import net.sf.saxon.TransformerFactoryImpl;

public class XMLConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(XMLConverter.class);

	public static String convertToXML(Object obj) throws JAXBException {
		return convertToXML(obj, false);
	}

	public static String convertToXML(Object obj, boolean prettyPrint) throws JAXBException {
		return convertToXML(obj, prettyPrint, false);
	}

	public static String convertToXML(Object obj, boolean prettyPrint, boolean ommitXmlDeclaration)
			throws JAXBException {
		String xml = null;

		StringWriter sw = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		if (prettyPrint) {
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		if (ommitXmlDeclaration) {
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}

		jaxbMarshaller.marshal(obj, sw);
		xml = sw.toString();
		return xml;
	}

	public static Node convertToXMLNode(Object obj) throws JAXBException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = dbf.newDocumentBuilder().newDocument();

		JAXBContext context = JAXBContext.newInstance(obj.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(obj, doc);

		return (Element) doc.getFirstChild();
	}

	public static <T> T fromXMLNamespaced(String xml, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringReader reader = new StringReader(xml);
		Source source = new StreamSource(reader);

		JAXBElement<T> root = unmarshaller.unmarshal(source, clazz);

		return root.getValue();
	}

	public static <T> T fromXML(String xml, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringReader reader = new StringReader(xml);

		return clazz.cast(unmarshaller.unmarshal(reader));
	}

	public static <T> T fromXML(Node node, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		return clazz.cast(unmarshaller.unmarshal(node));
	}

	public static String nodeToString(Node node) throws XMLConversionException {
		StringWriter sw = new StringWriter();
		try {

			TransformerFactory fact = new TransformerFactoryImpl();
			Transformer t = fact.newTransformer();

			// Transformer t =
			// TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error(te.getMessage(), te);
			throw new XMLConversionException("Error during XML to string conversion", te);
		}
		return sw.toString();
	}

	public static String nodeToString(Node node, boolean pretty) throws XMLConversionException {
		StringWriter sw = new StringWriter();
		try {
			// Transformer t =
			// TransformerFactory.newInstance().newTransformer();
			TransformerFactory fact = new TransformerFactoryImpl();
			Transformer t = fact.newTransformer();

			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			if (pretty)
				t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error(te.getMessage(), te);
			throw new XMLConversionException("Error during XML to string conversion", te);
		}
		return sw.toString();
	}

	public static Node stringToNode(String str) throws XMLConversionException {
		return stringToNode(str, true);
	}

	public static Node stringToNode(String str, boolean namespaceAware) throws XMLConversionException {
		if (str.isEmpty())
			return null;

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(namespaceAware);
			return documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(str.getBytes()))
					.getDocumentElement();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.error("Invalid XML text: " + str);
			throw new XMLConversionException("Error during string to XML conversion", e);
		}
	}

	public static List<Node> nodelist(final NodeList list) {
		return new AbstractList<Node>() {
			public int size() {
				return list.getLength();
			}

			public Node get(int index) {
				Node item = list.item(index);
				if (item == null)
					throw new IndexOutOfBoundsException();
				return item;
			}
		};
	}

	public static List<Node> nodelist(final NodeList list, short nodeType) {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < list.getLength(); ++i) {
			if (list.item(i).getNodeType() == nodeType) {
				nodes.add(list.item(i));
			}
		}
		return nodes;
	}

	/**
	 * 
	 * @param document
	 * @param localName
	 * @param namespaceURI
	 * @param prefix
	 *            if null the default prefix is used
	 * @return
	 */
	public static Node nodeFactory(Document document, String localName, String namespaceURI, String prefix) {
		Node node = document.createElementNS(namespaceURI, localName);
		if (prefix != null) {
			node.setPrefix(prefix);
		}
		return node;
	}

	public static void renameChildElements(Node parent, String oldNodeName, String newNodeName) throws XMLConversionException {
		for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
			Node oldNode = parent.getChildNodes().item(i);

			oldNodeName = oldNodeName.replace("<", "").replace(">", "");

			newNodeName = newNodeName.replace("<", "").replace(">", "");

			String value = StringEscapeUtils.escapeXml11(oldNode.getTextContent().trim());

			if (oldNode.getNodeName().equalsIgnoreCase(oldNodeName)) {
				Node newNode = createNode(newNodeName, value);
				parent.replaceChild(parent.getOwnerDocument().importNode(newNode, true), oldNode);
			}
		}
	}

	public static Node createNode(String tag, String value) throws XMLConversionException {
		return createNode(tag, value, true);
	}

	public static Node createNode(String tag, String value, boolean escape) throws XMLConversionException {

		tag = tag.replace("<", "").replace(">", "");

		if (escape)
			value = StringEscapeUtils.escapeXml11(value);

		Node newNode = XMLConverter.stringToNode("<" + tag + ">" + value + "</" + tag + ">");

		return newNode;
	}

	public static Node createNode(String tag, String namespaceUri, String namespacePrefix, String value) throws XMLConversionException {
		tag = tag.replace("<", "").replace(">", "");

		Node newNode = XMLConverter.stringToNode("<" + namespacePrefix + ":" + tag + " xmlns:" + namespacePrefix + "=\""
				+ namespaceUri + "\" >" + StringEscapeUtils.escapeXml11(value) + "</" + namespacePrefix + ":" + tag + ">", true);

		return newNode;
	}

	public static Node removeDuplicateChildNodes(Node node) {
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node childNode = node.getChildNodes().item(i);
			if (((Element) node).getElementsByTagName(childNode.getNodeName()).getLength() > 1) {
				for (int j = 0; j < ((Element) node).getElementsByTagName(childNode.getNodeName()).getLength(); j++) {
					Node oldChild = ((Element) node).getElementsByTagName(childNode.getNodeName()).item(j);
					if (!oldChild.equals(childNode)) {
						if (oldChild.isEqualNode(childNode)) {
							node.removeChild(oldChild);
						}
					}
				}
			}
		}
		return node;
	}
	
	public static Node removeEmptyNodes(Node node) throws XMLConversionException {
		try {
			XPathEvaluator evaluator = new XPathEvaluator(node);
			for (Node subNode : evaluator.evaluateToNode("//*[string-length(.) = 0]")) {
				subNode.getParentNode().removeChild(subNode);
			}
		} catch (XPathFactoryConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new XMLConversionException("Error during string to XML conversion", e);
		} catch (DOMException e) {
			logger.error(e.getMessage(), e);
			throw new XMLConversionException("Error during string to XML conversion", e);
		} catch (XPathEvaluationException e) {
			logger.error(e.getMessage(), e);
			throw new XMLConversionException("Error during string to XML conversion", e);
		}
		return node;
	}

}