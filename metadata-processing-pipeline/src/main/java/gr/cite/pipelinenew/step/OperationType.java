package gr.cite.pipelinenew.step;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = OperationTypeJsonDeserializer.class)
public enum OperationType {
	EXTRACT("extract"),
	ENRICH("enrich"),
	MAP("map"),
	FILTER("filter");
	
	private String type;
	
	OperationType(String type) {
		this.type = type;
	}
	
	public static OperationType fromType(String type) {
		switch (type) {
			case "map":
				return MAP;
			case "filter":
				return FILTER;
			case "extract":
				return EXTRACT;
			default:
				return MAP;
		}
	}
	
	public String getType() {
		return this.type;
	}
}

class OperationTypeJsonDeserializer extends JsonDeserializer<OperationType> {
	@Override
	public OperationType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		return OperationType.fromType(jsonParser.getText());
	}
}