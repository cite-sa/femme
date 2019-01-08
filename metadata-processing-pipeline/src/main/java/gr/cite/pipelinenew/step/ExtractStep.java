package gr.cite.pipelinenew.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.naming.OperationNotSupportedException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.util.List;

public class ExtractStep extends PipelineStep {
	private static final Logger logger = LoggerFactory.getLogger(ExtractStep.class);
	public static final ObjectMapper mapper = new ObjectMapper();
	
	private Format format;
	private String query;
	
	public Format getFormat() {
		return format;
	}
	
	public void setFormat(Format format) {
		this.format = format;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException {
		switch (this.getFormat()) {
			case XML:
				executeXPath((String)input, this.query);
			case JSON:
				executeJsonPath(input, this.query);
			default:
				throw new OperationNotSupportedException("Pipeline does not support processing for format " + getFormat());
		}
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
