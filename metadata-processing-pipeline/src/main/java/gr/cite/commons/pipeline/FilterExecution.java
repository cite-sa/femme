package gr.cite.commons.pipeline;

import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.w3c.dom.Node;

import javax.naming.OperationNotSupportedException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

final class FilterExecution {
	/*private enum Types {
		STRING("string"),
		INTEGER("integer"),
		DOUBLE("double"),
		DATE("date");

		private String type;

		Types(String type) {
			this.type = type;
		}
	}*/

	static List<String> applyQuery(String query, String input, String format) throws OperationNotSupportedException {
		if ("xml".equals(format)) {
			try {
				Node xml = XMLConverter.stringToNode(input);
				XPathEvaluator evaluator = new XPathEvaluator(xml);
				return evaluator.evaluate(query);
			} catch (XMLConversionException | XPathFactoryConfigurationException | XPathEvaluationException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new OperationNotSupportedException("Pipeline does not support processing for format " + format);
		}
	}

	static boolean applyOperator(List<String> queryResult, String operator, String value, Types type) throws OperationNotSupportedException {
		Class<?> typeClass = FilterExecution.mapTypeToClass(type);

		//FilterExecution.deserializeValue(result, type, typeClass);
		switch (operator) {
			case "=":
				return FilterExecution.applyEqual(queryResult, value, type, typeClass);
			case "!=":
				return FilterExecution.applyEqual(queryResult, value, type, typeClass);
			case ">":
				return FilterExecution.applyGreaterThan(queryResult, value, type, typeClass);
			case "<":
				return FilterExecution.applyEqual(queryResult, value, type, typeClass);
		}
		return false;
	}

	private static <T> boolean applyEqual(List<String> queryResult, String value, Types type, Class<T> typeClass) throws OperationNotSupportedException {
		switch (type) {
			case STRING:
				for (String result : queryResult) {
					if (FilterExecution.deserializeValue(result, type, typeClass).equals(FilterExecution.deserializeValue(value, type, typeClass))) {
						return true;
					}
				}
				break;
			case INTEGER:
				for (String result : queryResult) {
					if (FilterExecution.deserializeValue(result, type, typeClass) == FilterExecution.deserializeValue(value, type, typeClass)) {
						return true;
					}
				}
				break;
			case DOUBLE:

				for (String result : queryResult) {
					if (FilterExecution.deserializeValue(result, type, typeClass) == FilterExecution.deserializeValue(value, type, typeClass)) {
						return true;
					}
				}
				break;
			case DATE:
				for (String result : queryResult) {
					if (((LocalDateTime) FilterExecution.deserializeValue(result, type, typeClass)).isEqual((LocalDateTime) FilterExecution.deserializeValue(value, type, typeClass))) {
						return true;
					}
				}
				break;
			default:
				throw new OperationNotSupportedException();
		}
		return false;
	}

	private static <T> boolean applyGreaterThan(List<String> queryResult, String value, Types type, Class<T> typeClass) throws OperationNotSupportedException {
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
	}

	private static <T> T deserializeValue(String value, Types type, Class<T> typeClass) throws OperationNotSupportedException {
		switch (type) {
			case STRING:
				return typeClass.cast(value);
			case INTEGER:
				return typeClass.cast(Integer.parseInt(value));
			case DOUBLE:
				return typeClass.cast(Double.parseDouble(value));
			case DATE:
				return typeClass.cast(FilterExecution.parseDateTime(value));
			default:
				throw new OperationNotSupportedException();
		}
	}

	private static Class<?> mapTypeToClass(Types type) {
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

	private static LocalDateTime parseDateTime(String dateTime) {
		dateTime = dateTime.replaceFirst("^\"", "").replaceFirst("\"$", "");
		try {
			return ZonedDateTime.parse(dateTime).toLocalDateTime();
		} catch (DateTimeParseException e) {
			return LocalDateTime.parse(dateTime);
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, OperationNotSupportedException {
		boolean result = FilterExecution.applyOperator(Arrays.asList("2017-06-15T00:00","2017-06-16T00:00","2017-06-17T00:00"), ">", "2017-06-17T00:00", Types.DATE);
		System.out.println(result);
	}
}
