package gr.cite.pipelinenew.step.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import gr.cite.pipelinenew.step.Format;
import gr.cite.pipelinenew.step.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.naming.OperationNotSupportedException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapStep extends PipelineStep {
	private static final Logger logger = LoggerFactory.getLogger(MapStep.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private Format format;
	private Map<String, String> mappings;
	
	public Format getFormat() {
		return format;
	}
	
	public void setFormat(Format format) {
		this.format = format;
	}
	
	public Map<String, String> getMappings() {
		return mappings;
	}
	
	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException {
		return applyMappings(input);
	}
	
	private Map<String, Object> applyMappings(Object input) throws OperationNotSupportedException {
		Function<Map.Entry<String, String>, Object> transformFunction;
		switch (this.getFormat()) {
			case XML:
				transformFunction = (entry) -> executeXPath((String) input, entry.getValue());
				break;
			case JSON:
				transformFunction = (entry) -> executeJsonPath(input, entry.getValue());
				break;
			default:
				throw new OperationNotSupportedException("Pipeline does not support processing for format " + getFormat());
		}
		
		return this.mappings.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, transformFunction));
	}
	
	private Object executeXPath(String input, String query) {
		
		try {
			Node xml = XMLConverter.stringToNode(input);
			XPathEvaluator evaluator = new XPathEvaluator(xml);
			List<String> extracted = evaluator.evaluate(query);
			
			if (extracted.size() > 0) return extracted.get(0);
			else return "";
		} catch (XMLConversionException | XPathFactoryConfigurationException | XPathEvaluationException e) {
			//throw new ProcessingPipelineHandlerException("Error in extract execution", e);
			logger.error("Error in extract execution", e);
			return "";
		}
	}
	private Object executeJsonPath(Object input, String query) {
		try {
			JsonNode node = mapper.readTree(input.toString().getBytes());
			return node.at(query).asText();
		} catch (IOException e) {
			logger.error("Error in extract execution", e);
			return "";
		}
	}
}
