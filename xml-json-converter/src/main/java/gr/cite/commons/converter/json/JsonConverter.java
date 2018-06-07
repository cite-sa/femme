package gr.cite.commons.converter.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.Iterator;

public class JsonConverter {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String TEXT = "#text";
	
	public static String jsonToFemmeJson(String json) throws IOException {
		json = removeJsonWhitespace(json);
		return transformJsonToFemmeJson(json);
	}
	
	private static String transformJsonToFemmeJson(String json) throws IOException {
		JsonNode tree = mapper.readTree(json);
		transformJsonSubNodes(tree);
		return tree.toString();
	}
	
	private static JsonNode transformJsonSubNodes(JsonNode node) {
		if (! node.isArray()) {
			node.fields().forEachRemaining(entry -> {
				String field = entry.getKey();
				JsonNode value = entry.getValue();
				
				transformJsonSubNodes(value);
				
				if (value.isValueNode()) {
					transformValueNodeToFemmeJsonTextNode(node, field, value);
				} else if (value.isArray()) {
					transformArrayValueNodeToFemmeJsonArrayTextNode(node, field, value);
				}
			});
		}
		
		return node;
	}
	
	private static void transformValueNodeToFemmeJsonTextNode(JsonNode node, String field, JsonNode value) {
		((ObjectNode) node).set(field, createFemmeJsonTextNode(value));
	}
	
	private static void transformArrayValueNodeToFemmeJsonArrayTextNode(JsonNode node, String field, JsonNode value) {
		ArrayNode arrayNode = mapper.createArrayNode();
		
		value.elements().forEachRemaining(element -> {
			transformJsonSubNodes(element);
			
			if (element.isValueNode()) element = createFemmeJsonTextNode(element);
			arrayNode.add(element);
		});
		
		((ObjectNode) node).set(field, arrayNode);
	}
	
	private static ObjectNode createFemmeJsonTextNode(JsonNode value) {
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("#text", value.asText());
		return objectNode;
	}
	
	public static String femmeJsonToJson(String femmeJson) throws IOException {
		return transformFemmeJsonToJson(femmeJson);
	}
	
	private static String transformFemmeJsonToJson(String femmeJson) throws IOException {
		JsonNode tree = mapper.readTree(femmeJson);
		transformFemmeJsonSubNodes(tree);
		return tree.toString();
	}
	
	private static JsonNode transformFemmeJsonSubNodes(JsonNode node) {
		if (! node.isArray()) {
			node.fields().forEachRemaining(entry -> {
				String field = entry.getKey();
				JsonNode value = entry.getValue();
				
				//transformFemmeJsonSubNodes(value);
				
				if (isFemmeJsonValueNode(value)) {
					((ObjectNode) node).set(field, value.get(TEXT));
				}/* else if (value.isArray()) {
					transformArrayValueNodeToFemmeJsonArrayTextNode(node, field, value);
				}*/
				transformFemmeJsonSubNodes(value);
			});
		}
		
		return node;
	}
	
	private static boolean isFemmeJsonValueNode(JsonNode node) {
		boolean isValueNode = true;
		Iterator<String> fieldNamesIterator = node.fieldNames();
		
		while (fieldNamesIterator.hasNext()) {
			String fieldName = fieldNamesIterator.next();
			isValueNode = TEXT.equals(fieldName) && isValueNode;
		}
		
		return isValueNode;
	}
	
	private static String removeJsonWhitespace(String json) throws IOException {
		return mapper.readValue(json, JsonNode.class).toString();
		
	}
}
