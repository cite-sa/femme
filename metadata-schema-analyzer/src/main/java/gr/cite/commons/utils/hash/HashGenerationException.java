package gr.cite.commons.utils.hash;

public class HashGenerationException extends Exception
{
	public HashGenerationException() {
		super();
	}

	public HashGenerationException(String message) {
		super(message);
	}

	public HashGenerationException(Throwable cause) {
		super(cause);
	}

	public HashGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public HashGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
