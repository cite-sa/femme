package gr.cite.commons.pipeline.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = FilterOperationDeserializer.class)
public class FilterOperation {
	private String operator;
	private List<FilterOperand> operands;
	private List<FilterOperation> subOperations;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<FilterOperand> getOperands() {
		return operands;
	}

	public void setOperands(List<FilterOperand> operands) {
		this.operands = operands;
	}

	public List<FilterOperation> getSubOperations() {
		return subOperations;
	}

	public void setSubOperations(List<FilterOperation> subOperations) {
		this.subOperations = subOperations;
	}


}

class FilterOperationDeserializer extends JsonDeserializer {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public Object deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
		FilterOperation operation = new FilterOperation();
		JsonNode node = parser.readValueAsTree();

		String rootFieldName = node.fieldNames().next();
		if ("and".equals(rootFieldName) || "or".equals(rootFieldName)) {
			operation.setOperator(rootFieldName);
			operation.setOperands(new ArrayList<>());
			node.get(rootFieldName).forEach(arrayNode -> {
				try {
					operation.getOperands().add(FilterOperationDeserializer.deserializeFilterOperand(arrayNode));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} else {
			operation.setOperator("and");
			//operation.s
		}
		System.out.println();

		return operation;
	}

	private static FilterOperand deserializeFilterOperand(JsonNode operandNode) throws IOException {
		return mapper.readValue(operandNode.toString(), FilterOperand.class);
		/*FilterOperand operand = new FilterOperand();

		operandNode.fields().forEachRemaining(field -> {
			switch (field.getKey()) {
				case "format":
					operand.setFormat(field.getValue().asText());
					break;
				case "query":
					operand.setQuery(field.getValue().asText());
					break;
				case "operator":
					operand.setOperator(field.getValue().asText());
					break;
				case "value":
					operand.setValue(field.getValue().asText());
					break;
				case "type":
					operand.setType(field.getValue().asText());
					break;
			}
		});

		return operand;*/
	}
}


