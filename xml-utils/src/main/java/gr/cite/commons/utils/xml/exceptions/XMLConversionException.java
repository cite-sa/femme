package gr.cite.commons.utils.xml.exceptions;

public class XMLConversionException extends Exception {
	
	private static final long serialVersionUID = -2867069502273204032L;

	public XMLConversionException() {
		super();
	}

	public XMLConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public XMLConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public XMLConversionException(String message) {
		super(message);
	}

	public XMLConversionException(Throwable cause) {
		super(cause);
	}

}
