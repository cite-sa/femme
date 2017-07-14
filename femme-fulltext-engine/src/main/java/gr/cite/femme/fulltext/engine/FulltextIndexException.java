package gr.cite.femme.fulltext.engine;

public class FulltextIndexException extends Exception {

	public FulltextIndexException() {
		super();
	}

	public FulltextIndexException(String message) {
		super(message);
	}

	public FulltextIndexException(Throwable cause) {
		super(cause);
	}

	public FulltextIndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public FulltextIndexException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
