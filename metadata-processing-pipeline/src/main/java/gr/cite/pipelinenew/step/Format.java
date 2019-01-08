package gr.cite.pipelinenew.step;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = FormatJsonDeserializer.class)
public enum Format {
	XML("xml"),
	JSON("json");
	
	private String format;
	
	Format(String format) {
		this.format = format;
	}
	
	public static Format fromFormat(String format) {
		switch (format) {
			case "xml":
				return XML;
			case "json":
				return JSON;
			default:
				return XML;
		}
	}
	
	public String getFormat() {
		return this.format;
	}
}

class FormatJsonDeserializer extends JsonDeserializer<Format> {
	@Override
	public Format deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		return Format.fromFormat(jsonParser.getText());
	}
}