package gr.cite.commons.pipelinenew.handlers;

import gr.cite.commons.pipeline.operations.FilterOperation;
import gr.cite.commons.pipeline.operations.ProcessingPipelineOperation;
import gr.cite.commons.pipeline.operations.ProcessingPipelineOperation.Datatype;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class FilterHandler implements PipelineHandler {
	private FilterOperation operation;
	
	FilterHandler(FilterOperation operation) {
		this.operation = operation;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException {
		Class<?> typeClass = mapTypeToClass(this.operation.getDatatype());
		
		switch (this.operation.getOperator()) {
			case "=":
				return applyEqual(input, this.operation.getValue(), this.operation.getDatatype(), typeClass) ? input : null;
			/*case "!=":
				return applyEqual(input, value, type, typeClass);
			case ">":
				return applyGreaterThan(input, value, type, typeClass);
			case "<":
				return applyEqual(input, value, type, typeClass);*/
			default:
				throw new OperationNotSupportedException("Filter operator is not supported [" + this.operation.getOperator() + "]");
		}
	}
	
	private Class<?> mapTypeToClass(ProcessingPipelineOperation.Datatype type) {
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
		T deserializedInput = deserializeValue(value, type, typeClass);
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
	
	/*private static <T> boolean applyGreaterThan(List<String> queryResult, String value, Types type, Class<T> typeClass) throws OperationNotSupportedException {
		switch (type) {
			case STRING:
				for (String result : queryResult) {
					if (((String) FilterExecution.deserializeValue(result, type, typeClass)).compareTo((String) FilterExecution.deserializeValue(value, type, typeClass)) > 0) {
						return true;
					}
				}
				break;
			case INTEGER:
				for (String result : queryResult) {
					if (((Integer) FilterExecution.deserializeValue(result, type, typeClass)) > ((Integer) FilterExecution.deserializeValue(value, type, typeClass))) {
						return true;
					}
				}
				break;
			case DOUBLE:
				for (String result : queryResult) {
					if (((Double) FilterExecution.deserializeValue(result, type, typeClass)) > ((Double) FilterExecution.deserializeValue(value, type, typeClass))) {
						return true;
					}
				}
				break;
			case DATE:
				for (String result : queryResult) {
					if (((LocalDateTime) FilterExecution.deserializeValue(result, type, typeClass)).isAfter((LocalDateTime) FilterExecution.deserializeValue(value, type, typeClass))) {
						return true;
					}
				}
				break;
			default:
				throw new OperationNotSupportedException();
		}
		return false;
	}*/
	
	private <T> T deserializeValue(String value, Datatype type, Class<T> typeClass) throws OperationNotSupportedException {
		switch (type) {
			case STRING:
				return typeClass.cast(value);
			case INTEGER:
				return typeClass.cast(Integer.parseInt(value));
			case DOUBLE:
				return typeClass.cast(Double.parseDouble(value));
			case DATE:
				return typeClass.cast(parseDateTime(value));
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
