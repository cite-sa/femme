package gr.cite.commons.pipeline.exceptions;

public class ProcessingPipelineHandlerException extends Exception {
	public ProcessingPipelineHandlerException() {
		super();
	}
	public ProcessingPipelineHandlerException(String message) {
		super(message);
	}
	public ProcessingPipelineHandlerException(Throwable cause) {
		super(cause);
	}
	public ProcessingPipelineHandlerException(String message, Throwable cause) {
		super(message, cause);
	}
	public ProcessingPipelineHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}