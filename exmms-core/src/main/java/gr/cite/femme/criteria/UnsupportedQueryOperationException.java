package gr.cite.femme.criteria;

public class UnsupportedQueryOperationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4969116746288513571L;

	public UnsupportedQueryOperationException() {
		super();
	}

	public UnsupportedQueryOperationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedQueryOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedQueryOperationException(String message) {
		super(message);
	}

	public UnsupportedQueryOperationException(Throwable cause) {
		super(cause);
	}

	
}
