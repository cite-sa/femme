package gr.cite.femme.fulltext.engine.elasticsearch;

public class FulltextSearchException extends Exception {

	public FulltextSearchException() {
		super();
	}

	public FulltextSearchException(String message) {
		super(message);
	}

	public FulltextSearchException(Throwable cause) {
		super(cause);
	}

	public FulltextSearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public FulltextSearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
