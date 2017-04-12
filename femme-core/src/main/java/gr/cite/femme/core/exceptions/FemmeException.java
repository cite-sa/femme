package gr.cite.femme.core.exceptions;

public class FemmeException extends Exception {
	public FemmeException() {
		super();
	}

	public FemmeException(String message) {
		super(message);
	}

	public FemmeException(Throwable cause) {
		super(cause);
	}

	public FemmeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FemmeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
