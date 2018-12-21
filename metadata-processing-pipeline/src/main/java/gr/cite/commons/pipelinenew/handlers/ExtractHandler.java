package gr.cite.commons.pipelinenew.handlers;

import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.commons.pipeline.operations.ExtractOperation;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.w3c.dom.Node;

import javax.naming.OperationNotSupportedException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.util.List;

public class ExtractHandler implements PipelineHandler {
	//private ProcessingPipelineCtx ctx;
	private ExtractOperation operation;
	
	ExtractHandler(ExtractOperation operation) {
		//this.ctx = ctx;
		this.operation = operation;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException, ProcessingPipelineHandlerException {
		switch (this.operation.getFormat()) {
			case XML:
				try {
					Node xml = XMLConverter.stringToNode((String) input);
					XPathEvaluator evaluator = new XPathEvaluator(xml);
					List<String> extracted = evaluator.evaluate(this.operation.getQuery());
					
					if (extracted.size() > 0) return extracted.get(0);
					else return null;
				} catch (XMLConversionException | XPathFactoryConfigurationException | XPathEvaluationException e) {
					throw new ProcessingPipelineHandlerException("Error in extract execution", e);
				}
			default:
				throw new OperationNotSupportedException("Pipeline does not support processing for format " + this.operation.getFormat());
		}
		
	}
}
