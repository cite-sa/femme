package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

public class SemanticSearchException extends Exception {
	
	public SemanticSearchException() {
		super();
	}
	
	public SemanticSearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public SemanticSearchException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SemanticSearchException(String message) {
		super(message);
	}
	
	public SemanticSearchException(Throwable cause) {
		super(cause);
	}
}
