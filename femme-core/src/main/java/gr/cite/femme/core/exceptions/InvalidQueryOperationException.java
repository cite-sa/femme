package gr.cite.femme.core.exceptions;

public class InvalidQueryOperationException extends Exception {
	private static final long serialVersionUID = -4582643753234352494L;
	
	public InvalidQueryOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidQueryOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidQueryOperationException(String message) {
		super(message);
	}

	public InvalidQueryOperationException(Throwable cause) {
		super(cause);
	}
}
