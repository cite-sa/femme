package gr.cite.commons.pipelinenew.handlers;

import gr.cite.commons.pipeline.operations.ExtractOperation;
import gr.cite.commons.pipeline.operations.FilterOperation;
import gr.cite.commons.pipeline.operations.ProcessingPipelineOperation;
import gr.cite.commons.pipeline.operations.TransformOperation;

public class ProcessingPipelineHandlerFactory {
	
	public static PipelineHandler getHandler(ProcessingPipelineOperation operation) {
		switch (operation.getOperation()) {
			case EXTRACT:
				return new ExtractHandler((ExtractOperation) operation);
			case FILTER:
				return new FilterHandler((FilterOperation) operation);
			case TRANSFORM:
				return new TransformHandler((TransformOperation) operation);
			default:
				throw new IllegalArgumentException("Invalid pipeline operation defined [" + operation.getOperation() + "]");
		}
	}
}
