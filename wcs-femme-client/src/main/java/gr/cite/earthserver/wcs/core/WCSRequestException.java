package gr.cite.earthserver.wcs.core;

import java.util.concurrent.ExecutionException;

public class WCSRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4298931267393902413L;

	private String error;

	private int status;

	public WCSRequestException() {
		super();
	}

	public WCSRequestException(String error, int status) {
		super("status: " + status + ", " + error);
		this.error = error;
		this.status = status;
	}

	public WCSRequestException(Throwable cause, int status) {
		super(cause);
		this.status = status;
	}

	public WCSRequestException(ExecutionException e) {
		super(e);
	}

	public String getError() {
		return error;
	}

	public WCSRequestException setError(String error) {
		this.error = error;
		return this;
	}

	public int getStatus() {
		return status;
	}

	public WCSRequestException setStatus(int status) {
		this.status = status;
		return this;
	}

}
