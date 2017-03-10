package gr.cite.commons.metadata.analyzer.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.commons.metadata.analyzer.core.MetadataSchemaAnalysis;
import gr.cite.commons.utils.hash.MurmurHash3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JSONSchemaAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(JSONSchemaAnalyzer.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String NAMESPACES = "ns";
    private static final String ATTRIBUTES = "@";
    private static final String TEXT = "#text";

    public static MetadataSchemaAnalysis analyze(String json) throws JsonParseException, IOException {
        Set<JSONPath> terminalNodePaths = new HashSet<>();
        traverseTree(json, terminalNodePaths);

        /*List<JSONPath> sorted;
        sorted = terminalNodePaths.stream().collect(Collectors.toList());
        sorted.sort(Comparator.comparing(String::length).thenComparing(String::compareTo));*/

        //terminalNodePaths.forEach(System.out::println);
        /*sorted.forEach(System.out::println);*/
        return new MetadataSchemaAnalysis(terminalNodePaths);
    }

    private static Set<JSONPath> traverseTree(String json, Set<JSONPath> terminalNodePaths) throws IOException {
        mapper.readTree(json).fields().forEachRemaining(entry -> traverseNode(entry.getKey(), entry.getValue(), new StringBuilder(), terminalNodePaths));
        return terminalNodePaths;
    }

    private static Set<JSONPath> traverseNode(String key, JsonNode node, StringBuilder path, Set<JSONPath> terminalNodePaths) {
        if (key != null) {
            path.append(key);

            boolean isObjectsOnlyArrayNode = false;
            if (JsonNodeType.ARRAY.equals(node.getNodeType())) {
                List<Boolean> isObject = Collections.synchronizedList(new ArrayList<>());
                node.elements().forEachRemaining(arrayNode -> isObject.add(arrayNode.isValueNode()));
                isObjectsOnlyArrayNode = isObject.stream().filter(Boolean::booleanValue).collect(Collectors.toList()).size() == 0;
            }
            terminalNodePaths.add(new JSONPath(path.toString(), isObjectsOnlyArrayNode));
        }

        if (JsonNodeType.ARRAY.equals(node.getNodeType())) {
            node.elements().forEachRemaining(arrayNode -> traverseNode(null, arrayNode, new StringBuilder(path), terminalNodePaths));
            return terminalNodePaths;
        } else if (!isTerminalNode(node)) {
            path.append(".");

            Iterable<Map.Entry<String, JsonNode>> iterable = node::fields;
            Map<String, JsonNode> children = StreamSupport.stream(iterable.spliterator(), false)
                .filter(field ->
                        !JSONSchemaAnalyzer.ATTRIBUTES.equals(field.getKey())
                        && !JSONSchemaAnalyzer.NAMESPACES.equals(field.getKey())
                        && !JSONSchemaAnalyzer.TEXT.equals(field.getKey())
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            children.forEach((childKey, childNode) -> traverseNode(childKey, childNode, new StringBuilder(path), terminalNodePaths));
            return terminalNodePaths;
        } else {
            terminalNodePaths.add(new JSONPath(path.toString(), JsonNodeType.ARRAY.equals(node.getNodeType())));
            return terminalNodePaths;
        }
    }

    private static boolean isTerminalNode(JsonNode node) {
        Iterable<String> iterable = node::fieldNames;
        return node.isValueNode()
            || StreamSupport.stream(iterable.spliterator(), true)
                .filter(fieldName ->
                        !JSONSchemaAnalyzer.NAMESPACES.equals(fieldName) && !JSONSchemaAnalyzer.ATTRIBUTES.equals(fieldName) && !JSONSchemaAnalyzer.TEXT.equals(fieldName)
                ).collect(Collectors.toList()).size() == 0;
    }

    public static void main(String[] args) throws IOException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");

        String xml = webTarget
                .queryParam("service", "WCS")
                .queryParam("version", "2.0.1")
                .queryParam("request", "DescribeCoverage")
                .queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
                .request().get(String.class);

        String json = XmlJsonConverter.xmlToJson(xml);
        //System.out.println(json);
        MetadataSchemaAnalysis schemaAnalysis = JSONSchemaAnalyzer.analyze(json);
        //System.out.println(schemaAnalysis);
        schemaAnalysis.getSchema().forEach(System.out::println);
        /*String hashResult;
        hashResult = schemaAnalysis.hash();
        System.out.println(hashResult);
        hashResult = schemaAnalysis.hash();
        System.out.println(hashResult);*/

    }
}