package gr.cite.femme.datastore.exceptions;

public class InvalidCriteriaQueryOperation extends Exception {
	private static final long serialVersionUID = -4582643753234352494L;
	
	public InvalidCriteriaQueryOperation(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidCriteriaQueryOperation(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCriteriaQueryOperation(String message) {
		super(message);
	}

	public InvalidCriteriaQueryOperation(Throwable cause) {
		super(cause);
	}
}
