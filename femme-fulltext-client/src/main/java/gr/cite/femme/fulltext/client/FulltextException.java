package gr.cite.femme.fulltext.client;

public class FulltextException extends Exception {
	public FulltextException() {
		super();
	}
	
	public FulltextException(String message) {
		super(message);
	}
	
	public FulltextException(Throwable cause) {
		super(cause);
	}
	
	public FulltextException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FulltextException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
