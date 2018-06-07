package gr.cite.commons.converter.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class JsonParser {
	private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String DEFAULT_NAMESPACE = "_default";
	private static final String ATTRIBUTES = "@";
	private static final String NAMESPACES = "ns";
	private static final String TEXT = "#text";


	public String parseJson1(String json) throws IOException, XMLStreamException {
		JsonFactory jsonFactory = new JsonFactory();
		com.fasterxml.jackson.core.JsonParser jsonParser = jsonFactory.createParser(json);

		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		OutputStream result = new ByteArrayOutputStream();
		XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(new BufferedOutputStream(result));

		boolean isNamespaceNode = false;
		boolean isAttributeNode = false;
		Stack<Node> nodes = new Stack<>();
		String fieldName = "";

		jsonParser.nextToken();
		xmlWriter.writeStartDocument();

		while (!jsonParser.isClosed()) {
			JsonToken jsonToken = jsonParser.nextToken();

			if (jsonToken != null) {
				switch (jsonToken) {

					case START_OBJECT:
						isNamespaceNode = isNamespaceNode || fieldName.equals(NAMESPACES);
						isAttributeNode = isAttributeNode || fieldName.equals(ATTRIBUTES);

						if (!isNamespaceNode && !isAttributeNode) {
							if (!nodes.isEmpty()) {
								String name = nodes.peek().getFieldName();
								xmlWriter.writeStartElement(name);
							}
						}
						break;

					case FIELD_NAME:
						fieldName = jsonParser.getCurrentName();

						if (isNamespaceNode) {
							if (DEFAULT_NAMESPACE.equals(fieldName)) {
								xmlWriter.writeDefaultNamespace(jsonParser.nextTextValue());
							} else {
								xmlWriter.writeNamespace(jsonParser.getCurrentName(), jsonParser.nextTextValue());
							}
						} else if (isAttributeNode) {
							xmlWriter.writeAttribute(jsonParser.getCurrentName(), jsonParser.nextTextValue());
						} else if (fieldName.equals(TEXT)) {
							xmlWriter.writeCharacters(jsonParser.nextTextValue());
						} else {
							if (!fieldName.equals(NAMESPACES) && !fieldName.equals(ATTRIBUTES)) {
								nodes.push(new Node(fieldName, false));
							}
						}
						break;

					case END_OBJECT:
						if (isNamespaceNode) {
							isNamespaceNode = false;
						} else if (isAttributeNode) {
							isAttributeNode = false;
						} else {
							if (!nodes.isEmpty() && !nodes.peek().isArray()) {
								nodes.pop();
								try {
									xmlWriter.writeEndElement();
								} catch (Exception e) {
									xmlWriter.writeEndDocument();
								}
							} else if (!nodes.isEmpty() && nodes.peek().isArray()) {
								xmlWriter.writeEndElement();
							}
						}
						break;

					case START_ARRAY:
						Node node = nodes.pop();
						node.setArray(true);
						nodes.push(node);
						break;

					case END_ARRAY:
						if (nodes.peek().isArray()) {
							nodes.pop();
						}
						break;

					default:
						break;
				}
			}

		}

		xmlWriter.flush();
		xmlWriter.close();

		return result.toString();
	}

	public String parseJson(String json) throws IOException, XMLStreamException {
		JsonNode tree = mapper.readTree(json);

		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		OutputStream result = new ByteArrayOutputStream();
		XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(new BufferedOutputStream(result));

		//xmlWriter.writeStartDocument();

		tree.fieldNames().forEachRemaining(field -> {
			try {
				parseSubNodes(field, tree.get(field), xmlWriter);
			} catch (IOException | XMLStreamException e) {
				logger.error(e.getMessage(), e);
			}
		});

		//xmlWriter.writeEndDocument();

		xmlWriter.flush();
		xmlWriter.close();

		return result.toString();
	}

	public void parseSubNodes(String nodeName, JsonNode node, XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
		if (node.isArray()) {
			for (JsonNode arrayNode: node) {
				parseSubNodes(nodeName, arrayNode, xmlWriter);
			}
			return;
		}

		xmlWriter.writeStartElement(nodeName);

		JsonNode namespaces = node.get(NAMESPACES);
		JsonNode attributes = node.get(ATTRIBUTES);

		if (namespaces != null) {
			namespaces.fieldNames().forEachRemaining(namespace -> {
				try {
					if (namespace.equals(DEFAULT_NAMESPACE)) {
						xmlWriter.writeDefaultNamespace(namespaces.path(namespace).asText());
					} else {
						xmlWriter.writeNamespace(namespace, namespaces.path(namespace).asText());
					}
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			});
		}

		if (attributes != null) {
			attributes.fieldNames().forEachRemaining(attribute -> {
				try {
					xmlWriter.writeAttribute(attribute, attributes.path(attribute).asText());
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			});
		}

		 if (!node.isArray()) {
			node.fieldNames().forEachRemaining(field -> {
				if (! field.equals(NAMESPACES) && ! field.equals(ATTRIBUTES)) {
					if (field.equals("#text")) {
						try {
							xmlWriter.writeCharacters(node.path(field).asText());
						} catch (XMLStreamException e) {
							e.printStackTrace();
						}
					} else {
						try {
							parseSubNodes(field, node.path(field), xmlWriter);
						} catch (IOException | XMLStreamException e) {
							e.printStackTrace();
						}
					}
				}
			});
			xmlWriter.writeEndElement();
		}

	}
}
