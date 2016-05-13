package gr.cite.femme.datastore.exceptions;

public class MetadataStoreException extends Exception {
	private static final long serialVersionUID = -4332761685133035401L;

	public MetadataStoreException() {
		super();
	}

	public MetadataStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MetadataStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataStoreException(String message) {
		super(message);
	}

	public MetadataStoreException(Throwable cause) {
		super(cause);
	}
}
