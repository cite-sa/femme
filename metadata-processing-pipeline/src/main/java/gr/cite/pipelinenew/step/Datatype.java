package gr.cite.pipelinenew.step;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = DatatypeJsonDeserializer.class)
public enum Datatype {
	INTEGER("integer"),
	DOUBLE("double"),
	STRING("string"),
	DATE("date");
	
	private String datatype;
	
	Datatype(String datatype) {
		this.datatype = datatype;
	}
	
	public static Datatype fromDatatype(String datatype) {
		switch (datatype) {
			case "integer":
				return INTEGER;
			case "double":
				return DOUBLE;
			case "string":
				return STRING;
			case "date":
				return DATE;
			default:
				return STRING;
		}
	}
	
	public String getDatatype() {
		return this.datatype;
	}
	
	public Class<? extends Object> getDatatypeClass() {
		switch (datatype) {
			case "integer":
				return Integer.class;
			case "double":
				return Double.class;
			case "string":
				return String.class;
			default:
				throw new IllegalArgumentException("Datatype [" + datatype + "] not supported");
		}
	}
}

class DatatypeJsonDeserializer extends JsonDeserializer<Datatype> {
	@Override
	public Datatype deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		return Datatype.fromDatatype(jsonParser.getText());
	}
}