package gr.cite.femme.exceptions;

public class InvalidQueryOperation extends Exception {
	private static final long serialVersionUID = -4582643753234352494L;
	
	public InvalidQueryOperation(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidQueryOperation(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidQueryOperation(String message) {
		super(message);
	}

	public InvalidQueryOperation(Throwable cause) {
		super(cause);
	}
}
