package gr.cite.pipelinenew.step.filter;

import gr.cite.pipelinenew.step.Datatype;
import gr.cite.pipelinenew.step.PipelineStep;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class FilterStep extends PipelineStep {
	private Map<ConditionsOperator, List<FilterCondition>> conditions;
	
	public Map<ConditionsOperator, List<FilterCondition>> getConditions() {
		return conditions;
	}
	
	public void setConditions(Map<ConditionsOperator, List<FilterCondition>> conditions) {
		this.conditions = conditions;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException {
		Map<String, Object> inputMap = (Map<String, Object>) input;
		boolean result = true;
		
		for (Map.Entry<ConditionsOperator, List<FilterCondition>> booleanConditions: this.conditions.entrySet()) {
			result = result && applyConditions(inputMap, booleanConditions.getKey(), booleanConditions.getValue());
		}
		
		return ! result ? null : input;
	}
	
	private boolean applyConditions(Map<String, Object> input, ConditionsOperator logicalOperator, List<FilterCondition> conditions) throws OperationNotSupportedException {
		Boolean result = null;
		
		if (conditions.size() == 0) return false;
		
		for (FilterCondition condition: conditions) {
			result = applyLogicalOperator(logicalOperator, result, applyCondition(input, condition));
		}
		return result;
	}
	
	private boolean applyLogicalOperator(ConditionsOperator logicalOperator, Boolean value1, Boolean value2) throws OperationNotSupportedException {
		if (value1 == null) return value2;
		
		switch (logicalOperator) {
			case AND:
				return value1 && value2;
			case OR:
				return value1 || value2;
			default:
				throw new OperationNotSupportedException("Logical operator is not supported [" + logicalOperator.getOperator() + "]");
		}
	}
	
	private boolean applyCondition(Map<String, Object> input, FilterCondition condition) throws OperationNotSupportedException {
		Class<?> typeClass = mapTypeToClass(condition.getDatatype());
		
		switch (condition.getOperator()) {
			case "=":
				return applyEqual(input.get(condition.getField()), condition.getValue(), condition.getDatatype(), typeClass);
			/*case "!=":
				return applyEqual(input, value, type, typeClass);
			case ">":
				return applyGreaterThan(input, value, type, typeClass);
			case "<":
				return applyEqual(input, value, type, typeClass);*/
			default:
				throw new OperationNotSupportedException("Filter operator is not supported [" + condition.getOperator() + "]");
		}
	}
	
	private Class<?> mapTypeToClass(Datatype type) {
		switch (type) {
			case STRING:
				return String.class;
			case INTEGER:
				return Integer.class;
			case DOUBLE:
				return Double.class;
			case DATE:
				return LocalDateTime.class;
			default:
				return Object.class;
		}
	}
	
	private <T> boolean applyEqual(Object input, String value, Datatype type, Class<T> typeClass) throws OperationNotSupportedException {
		T deserializedInput = deserializeValue(input, type, typeClass);
		T deserializedValue = deserializeValue(value, type, typeClass);
		
		switch (type) {
			case STRING:
				return deserializedInput.equals(deserializedValue);
			case INTEGER:
				return deserializedInput == deserializedValue;
			case DOUBLE:
				return deserializedInput == deserializedValue;
			case DATE:
				return ((LocalDateTime) deserializedInput).isEqual((LocalDateTime) deserializedValue);
			default:
				throw new OperationNotSupportedException();
		}
	}
	
	private <T> T deserializeValue(Object value, Datatype type, Class<T> typeClass) throws OperationNotSupportedException {
		switch (type) {
			case STRING:
				return typeClass.cast(value);
			case INTEGER:
				return typeClass.cast(Integer.parseInt((String) value));
			case DOUBLE:
				return typeClass.cast(Double.parseDouble((String) value));
			case DATE:
				return typeClass.cast(parseDateTime((String) value));
			default:
				throw new OperationNotSupportedException();
		}
	}
	
	private LocalDateTime parseDateTime(String dateTime) {
		dateTime = dateTime.replaceFirst("^\"", "").replaceFirst("\"$", "");
		try {
			return ZonedDateTime.parse(dateTime).toLocalDateTime();
		} catch (DateTimeParseException e) {
			return LocalDateTime.parse(dateTime);
		}
	}
}
