package gr.cite.femme.query.serialization.mongodb;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = WhereCustomSerializer.class)
public class WhereSerializer {
	private static final Logger logger = LoggerFactory.getLogger(WhereSerializer.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@JsonProperty
	private String operator;
	
	@JsonProperty
	private String fieldName;
	
	@JsonProperty
	private OperatorSerializer operation;
	
	public WhereSerializer() {
		
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public OperatorSerializer getOperation() {
		return operation;
	}

	public void setOperation(OperatorSerializer operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "Error in serialization";
		}
	}

	public static WhereSerializer valueOf(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, WhereSerializer.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WhereSerializer other = (WhereSerializer) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		return true;
	}

	
	
}


class WhereCustomSerializer extends JsonSerializer<WhereSerializer> {

	@Override
	public void serialize(WhereSerializer value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		jgen.writeObject(value);
	}
}