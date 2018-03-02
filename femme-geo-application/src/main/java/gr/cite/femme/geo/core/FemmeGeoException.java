package gr.cite.femme.geo.core;

public class FemmeGeoException extends Exception {
	
	private static final long serialVersionUID = -8319064010838001776L;
	
	private Integer status;
	
	private Integer code;
	
	public FemmeGeoException() {
		super();
	}
	
	public FemmeGeoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public FemmeGeoException(String message, Integer status, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	public FemmeGeoException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FemmeGeoException(String message, Integer status) {
		super(message);
		this.status = status;
	}
	
	public FemmeGeoException(String message) {
		super(message);
	}
	
	public FemmeGeoException(Throwable cause) {
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
