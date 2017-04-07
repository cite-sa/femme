package gr.cite.commons.converter.json;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gr.cite.commons.converter.xml.XmlParser;

@JsonSerialize(using=JsonDocumentSerializer.class)
@JsonInclude(Include.NON_NULL)
public class JsonDocument {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonDocument.class);
	
	private JsonNode rootNode;
	public JsonNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(JsonNode rootNode) {
		this.rootNode = rootNode;
	}

	@Override
	public String toString() {
		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return json;
	}
}

class JsonDocumentSerializer extends JsonSerializer<JsonDocument> {
	@Override
	public void serialize(JsonDocument jsonDoc, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
		jsonGenerator.writeStartObject();

		if (jsonDoc != null) {
			if (jsonDoc.getRootNode() != null) {
				if (jsonDoc.getRootNode().getName() != null) {
					jsonGenerator.writeFieldName(jsonDoc.getRootNode().getName());
				}
				jsonGenerator.writeObject(jsonDoc.getRootNode());
			}
		}
		jsonGenerator.writeEndObject();
	}
}
