package gr.cite.femme.metadata.xpath.path.materializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.commons.converter.XmlJsonConverter;

public class PathMaterializer {
	
	private static final Logger logger = LoggerFactory.getLogger(PathMaterializer.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String NAMESPACES = "ns";
	
	private static final String ATTRIBUTES = "@";
	
	
	public static List<MaterializedPathsNode> materialize(String metadatumId, String json) throws JsonParseException, IOException {
		/*JsonParser parser = mapper.getFactory().createParser(json);*/
		List<MaterializedPathsNode> materializedPathsNodes = new ArrayList<>();
		/*parser.nextToken();*/
//		parseJson(null, parser, null, new StringBuilder(), materializedPathsNodes);
		traverseTree(json, materializedPathsNodes);
		
		//System.out.println(mongodbObjects);
		/*FileWriter writer = new FileWriter("/home/kapostolopoulos/Desktop/json-tree.json");
		for(String str: mongodbObjects) {
		  writer.write(str);
		}
		writer.close();*/
		
		
		
		
		/*while(!parser.isClosed()){
		    JsonToken jsonToken = parser.nextToken();

		    System.out.println("jsonToken = " + jsonToken);
		    System.out.println(parser.getText());
		}*/
		materializedPathsNodes.forEach(node -> node.setMetadatumId(metadatumId));
		return materializedPathsNodes;
	}
	
	private static void parseJson(String elementName, JsonParser parser, JsonGenerator generator, StringBuilder path, List<String> materialiedPathsNodes) throws IOException {
//		OutputStream mongodbObject = new ByteArrayOutputStream();
//		JsonGenerator generator = null;
		
		if (elementName != null && generator != null) {
			generator.writeObjectField("name", elementName);
			path.append("/" + elementName);
			generator.writeObjectField("path", path);
		}
		
		while (!parser.isClosed()) {
			JsonToken jsonToken = parser.nextToken(); 
			if (JsonToken.START_OBJECT.equals(jsonToken)) {
					//generator = mapper.getFactory().createGenerator(mongodbObject);
					//generator.writeStartObject();
//					OutputStream childMongodbObject = new ByteArrayOutputStream();
					JsonGenerator childGenerator = mapper.getFactory().createGenerator(new ByteArrayOutputStream());
					childGenerator.writeStartObject();
					
					parseJson(elementName, parser, childGenerator, new StringBuilder(path), materialiedPathsNodes);
			} else if (JsonToken.END_OBJECT.equals(jsonToken)) {
				if (generator != null) {
					generator.writeEndObject();
					generator.flush();
					materialiedPathsNodes.add(generator.getOutputTarget().toString());
				}
				return;
			} else if (JsonToken.FIELD_NAME.equals(jsonToken)) {
				if (PathMaterializer.NAMESPACES.equals(parser.getText()) || PathMaterializer.ATTRIBUTES.equals(parser.getText())) {
					generator.writeFieldName(parser.getText());
					jsonToken = parser.nextToken();
					generator.writeTree(parser.readValueAsTree());
				} else {
					elementName = parser.getText();
					/*if (generator == null) {*/
//					generator.writeStartObject();
					//generator.writeObjectField("name", parser.getText());

					/*System.out.println(parser.getText());*/

					//generator.writeObjectField("path", path.append("/" + parser.getText()).toString());
					//jsonToken = parser.nextToken();
					/*} else {
						parseJson(elementName, parser, path, mongodbObjects);	
					}*/
				}
			} else if (JsonToken.VALUE_STRING.equals(jsonToken)) {
//				OutputStream tempMongodbObject = new ByteArrayOutputStream();
				JsonGenerator tempGenerator = mapper.getFactory().createGenerator(new ByteArrayOutputStream());
				tempGenerator.writeStartObject();
				tempGenerator.writeObjectField("name", elementName);
				tempGenerator.writeObjectField("value", parser.getText());
				path.append("/" + elementName);
				tempGenerator.writeObjectField("path", path);
				tempGenerator.writeEndObject();
				tempGenerator.flush();
				materialiedPathsNodes.add(tempGenerator.getOutputTarget().toString());
			}
		   
		   
		   

		    /*System.out.println("jsonToken = " + jsonToken);
		    System.out.println(parser.getText());
		    if (PathMaterializer.NAMESPACES.equals(parser.getText())) {
		    	jsonToken = parser.nextToken();
		    	TreeNode treeNode = parser.readValueAsTree();
		    	System.out.println(treeNode.toString());
		    }*/
		    
		}
	}

	private static void traverseTree(String json, List<MaterializedPathsNode> nodes) throws IOException {
		mapper.readTree(json).fields().forEachRemaining((Map.Entry<String, JsonNode> entry) -> {
			traverseNode(entry.getValue(), null, entry.getKey(), new StringBuilder(), nodes);
		});
	}

	private static List<String> traverseNode(JsonNode node, String parentId, String name, StringBuilder path, List<MaterializedPathsNode> nodes) {
		List<String> childrenIds = new ArrayList<>();

		MaterializedPathsNode pathsNode = new MaterializedPathsNode();
		pathsNode.setId(new ObjectId().toString());
		pathsNode.setName(name);
		pathsNode.setPath(path.append("/" + name).toString());
		pathsNode.setParent(parentId);
		pathsNode.setChildren(new ArrayList<>());

//		System.out.println(node.getNodeType());
		if (JsonNodeType.ARRAY.equals(node.getNodeType())) {
			node.elements().forEachRemaining(arrayNode -> {
				List<String> childId = traverseNode(arrayNode, parentId, name, new StringBuilder(path), nodes);
				childrenIds.addAll(childId);
			});
			return childrenIds;
		} else if (!JsonNodeType.OBJECT.equals(node.getNodeType())) {
			pathsNode.setValue(node.asText());

		}

		node.fields().forEachRemaining((Map.Entry<String, JsonNode> entry) -> {
			if (PathMaterializer.NAMESPACES.equals(entry.getKey())) {
				Map<String, String> namespaces = new HashMap<>();
				entry.getValue().fields().forEachRemaining(nsEntry -> namespaces.put(nsEntry.getKey(), nsEntry.getValue().textValue()));
				pathsNode.setNamespaces(namespaces);

			} else if (PathMaterializer.ATTRIBUTES.equals(entry.getKey())) {
				Map<String, String> attributes = new HashMap<>();
				entry.getValue().fields().forEachRemaining(attrEntry -> attributes.put(attrEntry.getKey(), attrEntry.getValue().textValue()));
				pathsNode.setAttributes(attributes);
			} else {
				List<String> childId = traverseNode(entry.getValue(), pathsNode.getId(), entry.getKey(), new StringBuilder(path), nodes);
				pathsNode.getChildren().addAll(childId);
			}
		});

		nodes.add(pathsNode);
		/*try {
			System.out.println(mapper.writeValueAsString(pathsNode));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}*/

		return Arrays.asList(pathsNode.getId());
	}
	
	public static void main(String[] args) throws JsonParseException, IOException {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("http://access.planetserver.eu:8080/rasdaman/ows");

		String xml = webTarget
			 .queryParam("service", "WCS")
			 .queryParam("version", "2.0.1")
			 .queryParam("request", "DescribeCoverage")
			 .queryParam("coverageId", "hrl0000c067_07_if185l_trr3")
			 .request().get(String.class);

		String json = XmlJsonConverter.xmlToJson(xml);

		//		 System.out.println(json);

		//		 PathMaterializer tree = new PathMaterializer();
		List<MaterializedPathsNode> metarializedPathsNodes = PathMaterializer.materialize(new ObjectId().toString(), json);
	}
}
