package gr.cite.mongodb.xpath.jsontree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.commons.converter.XmlJsonConverter;

public class TreeConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(TreeConverter.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String NAMESPACES = "ns";
	
	private static final String ATTRIBUTES = "@";
	
	
	public void flatten(String json) throws JsonParseException, IOException {
		JsonParser parser = mapper.getFactory().createParser(json);
		List<String> mongodbObjects = new ArrayList<>();
		parser.nextToken();
		parseJson(null, parser, null, new StringBuilder(), mongodbObjects);
		
		//System.out.println(mongodbObjects);
		FileWriter writer = new FileWriter("/home/kapostolopoulos/Desktop/json-tree.json"); 
		for(String str: mongodbObjects) {
		  writer.write(str);
		}
		writer.close();
		
		
		
		
		/*while(!parser.isClosed()){
		    JsonToken jsonToken = parser.nextToken();

		    System.out.println("jsonToken = " + jsonToken);
		    System.out.println(parser.getText());
		}*/

		
	}
	
	private void parseJson(String elementName, JsonParser parser, JsonGenerator generator, StringBuilder path, List<String> mongodbObjects) throws IOException {
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
					
					parseJson(elementName, parser, childGenerator, new StringBuilder(path), mongodbObjects);
			} else if (JsonToken.END_OBJECT.equals(jsonToken)) {
				if (generator != null) {
					generator.writeEndObject();
					generator.flush();
					mongodbObjects.add(generator.getOutputTarget().toString());
				}
				return;
			} else if (JsonToken.FIELD_NAME.equals(jsonToken)) {
				if (TreeConverter.NAMESPACES.equals(parser.getText()) || TreeConverter.ATTRIBUTES.equals(parser.getText())) {
					generator.writeFieldName(parser.getText());
					jsonToken = parser.nextToken();
					generator.writeTree(parser.readValueAsTree());
				} else {
					elementName = parser.getText();
					/*if (generator == null) {*/
//					generator.writeStartObject();
					//generator.writeObjectField("name", parser.getText());
					System.out.println(parser.getText());
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
				mongodbObjects.add(tempGenerator.getOutputTarget().toString());
			}
		   
		   
		   

		    /*System.out.println("jsonToken = " + jsonToken);
		    System.out.println(parser.getText());
		    if (TreeConverter.NAMESPACES.equals(parser.getText())) {
		    	jsonToken = parser.nextToken();
		    	TreeNode treeNode = parser.readValueAsTree();
		    	System.out.println(treeNode.toString());
		    }*/
		    
		}
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
		 
		 TreeConverter tree = new TreeConverter();
		 tree.flatten(json);
	}
}
