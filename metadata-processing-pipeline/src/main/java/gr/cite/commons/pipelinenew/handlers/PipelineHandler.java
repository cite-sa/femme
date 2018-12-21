package gr.cite.commons.pipelinenew.handlers;

import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;

import javax.naming.OperationNotSupportedException;

public interface PipelineHandler {
	Object process(Object input) throws OperationNotSupportedException, ProcessingPipelineHandlerException;
}
