package gr.cite.femme.application.exception;

public class FemmeApplicationException extends Exception {

	private static final long serialVersionUID = -8319064010838001776L;
	
	private Integer status;
	
	private Integer code;
	
	public FemmeApplicationException() {
		super();
	}

	public FemmeApplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FemmeApplicationException(String message, Integer status, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	public FemmeApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FemmeApplicationException(String message, Integer status) {
		super(message);
		this.status = status;
	}
	
	public FemmeApplicationException(String message) {
		super(message);
	}

	public FemmeApplicationException(Throwable cause) {
		super(cause);
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
}
