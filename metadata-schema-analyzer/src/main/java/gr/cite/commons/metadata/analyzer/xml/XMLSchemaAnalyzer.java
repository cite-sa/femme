package gr.cite.commons.metadata.analyzer.xml;

import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.LinkedHashMap;

public class XMLSchemaAnalyzer {

    public static Object analyze(String xml) {

        XMLStreamReader streamReader;
        try {
            streamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));

            while (streamReader.hasNext()) {
                streamReader.next();
                switch (streamReader.getEventType()) {
                    case XMLStreamReader.START_ELEMENT:
                        /*jsonNode = xmlElementToJsonNode(streamReader, jsonNode);*/
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        /*jsonNode = jsonNode.getParent();*/
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
                            /*jsonNode.setText(text);*/
                        }

                        break;
                    case XMLStreamReader.END_DOCUMENT:
                        /*logger.debug("XML document parsing completed.");*/
                        break;
                    default:
                        /*logger.error("Invalid event type.");*/
                        break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void xmlElementToJsonNode(XMLStreamReader streamReader) {
        LinkedHashMap<String, String> attributes = null, namespaces = null;
        /*XmlJsonNode tempNode = new XmlJsonNode();*/

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
                    namespacePrefix = "_default";
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

        /*tempNode.setName(name);
        tempNode.setNamespaces(namespaces);
        tempNode.setAttributes(attributes);

        tempNode.setParent(jsonNode);

        if (jsonNode != null) {
            jsonNode.addChild(tempNode);
        } else {
            jsonDoc.setRootNode(tempNode);
        }

        return tempNode;*/
    }
}
