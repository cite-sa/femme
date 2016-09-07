package gr.cite.femme.client;

public class FemmeDatastoreException extends Exception {
	
	private static final long serialVersionUID = 1474149125975041240L;

	public FemmeDatastoreException() {
		super();
	}

	public FemmeDatastoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FemmeDatastoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public FemmeDatastoreException(String message) {
		super(message);
	}

	public FemmeDatastoreException(Throwable cause) {
		super(cause);
	}
}
