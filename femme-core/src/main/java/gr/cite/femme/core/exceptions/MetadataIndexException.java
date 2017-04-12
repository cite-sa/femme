package gr.cite.femme.core.exceptions;

public class MetadataIndexException extends Exception {

	public MetadataIndexException() {
		super();
	}

	public MetadataIndexException(String message) {
		super(message);
	}

	public MetadataIndexException(Throwable cause) {
		super(cause);
	}

	public MetadataIndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataIndexException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
