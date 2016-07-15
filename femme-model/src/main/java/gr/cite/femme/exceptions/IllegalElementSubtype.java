package gr.cite.femme.exceptions;

public class IllegalElementSubtype extends Exception {
	private static final long serialVersionUID = 7301669818899482422L;

	public IllegalElementSubtype(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalElementSubtype(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalElementSubtype(String message) {
		super(message);
	}

	public IllegalElementSubtype(Throwable cause) {
		super(cause);
	}
}
