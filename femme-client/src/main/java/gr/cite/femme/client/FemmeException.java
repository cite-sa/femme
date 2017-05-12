package gr.cite.femme.client;

public class FemmeException extends Exception {
	
	private static final long serialVersionUID = 1474149125975041240L;

	public FemmeException() {
		super();
	}

	public FemmeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FemmeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FemmeException(String message) {
		super(message);
	}

	public FemmeException(Throwable cause) {
		super(cause);
	}
}
