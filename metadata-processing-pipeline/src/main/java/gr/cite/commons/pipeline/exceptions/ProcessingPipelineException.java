package gr.cite.commons.pipeline.exceptions;

public class ProcessingPipelineException extends Exception {
	public ProcessingPipelineException() {
		super();
	}
	public ProcessingPipelineException(String message) {
		super(message);
	}
	public ProcessingPipelineException(Throwable cause) {
		super(cause);
	}
	public ProcessingPipelineException(String message, Throwable cause) {
		super(message, cause);
	}
	public ProcessingPipelineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}