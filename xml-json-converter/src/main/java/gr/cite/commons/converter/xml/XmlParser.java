package gr.cite.commons.converter.xml;

import java.io.StringReader;
import java.util.LinkedHashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.commons.converter.json.JsonDocument;
import gr.cite.commons.converter.json.JsonNode;

public class XmlParser {

	private static final Logger logger = LoggerFactory.getLogger(XmlParser.class);

	private static final String DEFAULT_NAMESPACE = "_default";

	private JsonDocument jsonDoc;

	public JsonDocument parseXml(String xml) throws XMLStreamException {
		jsonDoc = new JsonDocument();
		JsonNode jsonNode = null;

		XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));

		while (streamReader.hasNext()) {
			streamReader.next();
			switch (streamReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					jsonNode = xmlElementToJsonNode(streamReader, jsonNode);
					break;
				case XMLStreamReader.END_ELEMENT:
					jsonNode = jsonNode.getParent();
					break;
				case XMLStreamReader.ATTRIBUTE:
					System.out.println("ATTRIBUTE");
					break;
				case XMLStreamReader.CDATA:
					System.out.println("CDATA");
					break;
				case XMLStreamReader.NAMESPACE:
					System.out.println("NAMESPACE");
					break;
				case XMLStreamReader.CHARACTERS:
					String text = streamReader.getText().trim();
					if (StringUtils.isBlank(text)) {
						break;
					} else {
						jsonNode.setText(text);
					}
					break;
				case XMLStreamReader.END_DOCUMENT:
					logger.debug("XML document parsing completed.");
					break;
				default:
					logger.error("Invalid event type.");
					break;
			}
		}
		return jsonDoc;
	}

	private JsonNode xmlElementToJsonNode(XMLStreamReader streamReader, JsonNode jsonNode) {
		LinkedHashMap<String, String> attributes = null, namespaces = null;
		JsonNode tempNode = new JsonNode();

		StringBuilder nameBuilder = new StringBuilder();
		if (!StringUtils.isBlank(streamReader.getPrefix())) {
			nameBuilder.append(streamReader.getPrefix());
			nameBuilder.append(":");
		}
		String name = nameBuilder.append(streamReader.getLocalName()).toString();

		if (streamReader.getNamespaceCount() > 0) {
			namespaces = new LinkedHashMap<>();
			for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
				String namespacePrefix = streamReader.getNamespacePrefix(i);
				if (StringUtils.isBlank(namespacePrefix)) {
					namespacePrefix = XmlParser.DEFAULT_NAMESPACE;
				}
				namespaces.put(namespacePrefix, streamReader.getNamespaceURI(i));
			}
		}
		
		if (streamReader.getAttributeCount() > 0) {
			attributes = new LinkedHashMap<>();
			for (int i = 0; i < streamReader.getAttributeCount(); i++) {
				StringBuilder attributeNameBuilder = new StringBuilder();
				if (!StringUtils.isBlank(streamReader.getAttributePrefix(i))) {
					attributeNameBuilder.append(streamReader.getAttributePrefix(i));
					attributeNameBuilder.append(":");
				}
				String attributeName = attributeNameBuilder.append(streamReader.getAttributeLocalName(i)).toString();
				attributes.put(attributeName, streamReader.getAttributeValue(i));
			}
		}

		tempNode.setName(name);
		tempNode.setNamespaces(namespaces);
		tempNode.setAttributes(attributes);

		tempNode.setParent(jsonNode);

		if (jsonNode != null) {
			jsonNode.addChild(tempNode);
		} else {
			jsonDoc.setRootNode(tempNode);
		}

		return tempNode;
	}
}
