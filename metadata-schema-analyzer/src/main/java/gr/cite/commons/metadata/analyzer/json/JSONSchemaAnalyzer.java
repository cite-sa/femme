package gr.cite.commons.metadata.analyzer.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.commons.metadata.analyzer.core.MetadataSchemaAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JSONSchemaAnalyzer {
	private static final Logger logger = LoggerFactory.getLogger(JSONSchemaAnalyzer.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String NAMESPACES = "ns";
	private static final String ATTRIBUTES = "@";
	private static final String TEXT = "#text";
	
	public static MetadataSchemaAnalysis analyze(String json) throws IOException {
		Set<JSONPath> terminalNodePaths = new HashSet<>();
		traverseTree(json, terminalNodePaths);
		
		return new MetadataSchemaAnalysis(terminalNodePaths);
	}
	
	private static Set<JSONPath> traverseTree(String json, Set<JSONPath> terminalNodePaths) throws IOException {
		mapper.readTree(json).fields().forEachRemaining(entry -> traverseNode(entry.getKey(), entry.getValue(), new StringBuilder(), terminalNodePaths));
		return terminalNodePaths;
	}
	
	private static Set<JSONPath> traverseNode(String key, JsonNode node, StringBuilder path, Set<JSONPath> terminalNodePaths) {
		if (key != null) {
			path.append(key);
			boolean isObjectsOnlyArrayNode = JsonNodeType.ARRAY.equals(node.getNodeType()) && isObjectOnlyArrayNode(node);
			terminalNodePaths.add(new JSONPath(path.toString(), isObjectsOnlyArrayNode));
		}
		
		if (JsonNodeType.ARRAY.equals(node.getNodeType())) {
			node.elements().forEachRemaining(arrayNode -> {
					if (! isTerminalNode(arrayNode)) traverseNode(null, arrayNode, new StringBuilder(path), terminalNodePaths);
			});
			return terminalNodePaths;
		} else if (! isTerminalNode(node)) {
			path.append(".");
			getChildrenObjectNodes(node).forEach((childKey, childNode) -> traverseNode(childKey, childNode, new StringBuilder(path), terminalNodePaths));
			return terminalNodePaths;
		} else {
			terminalNodePaths.add(new JSONPath(path.toString(), JsonNodeType.ARRAY.equals(node.getNodeType())));
			return terminalNodePaths;
		}
	}
	
	private static boolean isObjectOnlyArrayNode(JsonNode node) {
		List<Boolean> isObject = Collections.synchronizedList(new ArrayList<>());
		node.elements().forEachRemaining(arrayNode -> isObject.add(arrayNode.isValueNode()));
		return isObject.stream().filter(Boolean::booleanValue).collect(Collectors.toList()).size() == 0;
	}
	
	private static Map<String, JsonNode> getChildrenObjectNodes(JsonNode node) {
		Iterable<Map.Entry<String, JsonNode>> iterable = node::fields;
		return StreamSupport.stream(iterable.spliterator(), false)
				   .filter(field -> isTerminalField(field.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	private static boolean isTerminalNode(JsonNode node) {
		Iterable<String> iterable = node::fieldNames;
		return node.isValueNode() ||
				   StreamSupport.stream(iterable.spliterator(), true).filter(JSONSchemaAnalyzer::isTerminalField).collect(Collectors.toList()).size() == 0;
	}
	
	private static boolean isTerminalField(String fieldName) {
		return ! NAMESPACES.equals(fieldName) && ! ATTRIBUTES.equals(fieldName) && ! TEXT.equals(fieldName);
	}
	
	public static void main(String[] args) throws IOException, XMLStreamException {
        /*Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");

        String xml = webTarget
                .queryParam("service", "WCS")
                .queryParam("version", "2.0.1")
                .queryParam("request", "DescribeCoverage")
                .queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
                .request().get(String.class);

        String json = XmlJsonConverter.xmlToFemmeJson(xml);*/
		String json = "{\"oai_dc:dc\":{\"ns\":{\"oai_dc\":\"http://www.openarchives.org/OAI/2.0/oai_dc/\",\"_default\":\"http://www.openarchives.org/OAI/2.0/\",\"dcmitype\":\"http://purl.org/dc/dcmitype/\",\"dcterms\":\"http://purl.org/dc/terms/\",\"xsi\":\"http://www.w3.org/2001/XMLSchema-instance\",\"dc\":\"http://purl.org/dc/elements/1.1/\"},\"dc:description\":[{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Widder (X 4952).\"},{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Ph_BAAE n. 159_169\"}],\"dc:title\":[{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Widder (X 4952).\"},{\"@\":{\"xml:lang\":\"en\"},\"#text\":\"Untitled\"}],\"dc:rights\":[{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Η εν Αθήναις Αρχαιολογική Εταιρεία\"},{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"https://creativecommons.org/licenses/by-nc-nd/4.0/deed.el\"},{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Creative Commons Αναφορά Μη Εμπορική Χρήση Όχι Παράγωγα Έργα 4.0\"}],\"dc:language\":{\"#text\":\"gre\"},\"dc:creator\":{\"#text\":\"Άγνωστος δημιουργός\"},\"dc:identifier\":{\"#text\":\"http://hdl.handle.net/20.500.11749/DOI-11673\"},\"dc:type\":{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Φωτογραφία\"},\"dc:source\":{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"W. Sch?rmann, Das Heiligtum des Hermes und der Aphrodite in Syme Viannou II, die Tierstatuetten aus Metal, Taf. 12 (131), ΒΑΕ 159, (Athen 1996).\"},\"dc:coverage\":{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Ελλάδα,Κρήτη,Νομός Ηρακλείου,Σύμη Βιάννου\"},\"dc:contributor\":{\"@\":{\"xml:lang\":\"el\"},\"#text\":\"Αγγελική Λεμπέση\"}}}";
		//System.out.println(json);
		MetadataSchemaAnalysis schemaAnalysis = JSONSchemaAnalyzer.analyze(json);
		//System.out.println(schemaAnalysis);
		schemaAnalysis.getSchema().forEach(System.out::println);
        /*String hashResult;
        hashResult = schemaAnalysis.getChecksum();
        System.out.println(hashResult);
        hashResult = schemaAnalysis.getChecksum();
        System.out.println(hashResult);*/
		
	}
}