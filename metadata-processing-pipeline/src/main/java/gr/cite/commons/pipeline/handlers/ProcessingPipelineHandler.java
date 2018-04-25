package gr.cite.commons.pipeline.handlers;

import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;

import javax.naming.OperationNotSupportedException;

public interface ProcessingPipelineHandler {
	Object process(Object input) throws OperationNotSupportedException, ProcessingPipelineHandlerException;
}
