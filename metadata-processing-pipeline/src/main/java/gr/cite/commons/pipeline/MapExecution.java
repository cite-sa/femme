package gr.cite.commons.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.pipeline.config.MapOperation;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.w3c.dom.Node;

import javax.naming.OperationNotSupportedException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapExecution {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static List<String> applyQuery(String query, String input, String format) throws OperationNotSupportedException, IOException {
		if ("xml".equals(format)) {
			try {
				Node xml = XMLConverter.stringToNode(input);
				XPathEvaluator evaluator = new XPathEvaluator(xml);
				return evaluator.evaluate(query);
			} catch (XMLConversionException | XPathFactoryConfigurationException | XPathEvaluationException e) {
				throw new RuntimeException(e);
			}
		} else if ("json".equals(format)) {
			JsonNode node = mapper.readTree(input);
			return Collections.singletonList(node.at(query).asText());
		} else {
			throw new OperationNotSupportedException("Pipeline does not support processing for format " + format);
		}
	}

}
