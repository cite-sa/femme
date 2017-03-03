package gr.cite.femme.exceptions;

public class DatastoreException extends Exception {
	private static final long serialVersionUID = -31368528254778068L;

	public DatastoreException() {
		super();
	}

	public DatastoreException(String message) {
		super(message);
	}

	public DatastoreException(Throwable cause) {
		super(cause);
	}

	public DatastoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatastoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
