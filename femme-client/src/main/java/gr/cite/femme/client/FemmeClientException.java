package gr.cite.femme.client;

public class FemmeClientException extends Exception {

	private static final long serialVersionUID = 809592623090498327L;

	public FemmeClientException() {
		super();
	}

	public FemmeClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FemmeClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public FemmeClientException(String message) {
		super(message);
	}

	public FemmeClientException(Throwable cause) {
		super(cause);
	}

}
