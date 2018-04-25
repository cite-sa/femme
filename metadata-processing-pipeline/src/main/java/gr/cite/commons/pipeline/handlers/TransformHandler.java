package gr.cite.commons.pipeline.handlers;

import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.commons.pipeline.operations.TransformOperation;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDateTime;

public class TransformHandler implements ProcessingPipelineHandler {
	private TransformOperation operation;
	
	public TransformHandler(TransformOperation operation) {
		this.operation = operation;
	}
	
	@Override
	public Object process(Object input) throws OperationNotSupportedException, ProcessingPipelineHandlerException {
		String inputString = (String) input;
		
		switch (this.operation.getDatatype()) {
			case INTEGER:
				return Integer.parseInt(inputString);
			case DOUBLE:
				return Double.parseDouble(inputString);
			case DATE:
				return LocalDateTime.parse(inputString);
			case STRING:
				return inputString;
			default:
				throw new OperationNotSupportedException("Datatype not supported [" + this.operation.getDatatype().name() + "]");
		}
	}
}
