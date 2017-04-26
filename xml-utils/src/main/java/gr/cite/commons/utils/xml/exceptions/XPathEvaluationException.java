package gr.cite.commons.utils.xml.exceptions;

public class XPathEvaluationException extends Exception {
	
	private static final long serialVersionUID = 421958340978081418L;

	public XPathEvaluationException() {
		super();
	}

	public XPathEvaluationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public XPathEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XPathEvaluationException(String message) {
		super(message);
	}

	public XPathEvaluationException(Throwable cause) {
		super(cause);
	}
}
