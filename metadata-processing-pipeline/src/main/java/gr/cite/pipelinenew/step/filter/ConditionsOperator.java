package gr.cite.pipelinenew.step.filter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = ConditionsOperatorJsonDeserializer.class)
public enum ConditionsOperator {
	AND("and"),
	OR("or"),
	NOT("not");
	
	private String operator;
	
	ConditionsOperator(String operator) {
		this.operator = operator;
	}
	
	public static ConditionsOperator fromOperator(String operator) {
		switch (operator) {
			case "and":
				return AND;
			case "or":
				return OR;
			case "not":
				return NOT;
			default:
				return AND;
		}
	}
	
	public String getOperator() {
		return this.operator;
	}
}

class ConditionsOperatorJsonDeserializer extends JsonDeserializer<ConditionsOperator> {
	@Override
	public ConditionsOperator deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		return ConditionsOperator.fromOperator(jsonParser.getText());
	}
}