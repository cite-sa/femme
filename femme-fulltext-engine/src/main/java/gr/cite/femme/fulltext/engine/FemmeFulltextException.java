package gr.cite.femme.fulltext.engine;

public class FemmeFulltextException extends Exception {
	
	private static final long serialVersionUID = -8319064010838001776L;
	
	private Integer status;
	
	private Integer code;
	
	public FemmeFulltextException() {
		super();
	}
	
	public FemmeFulltextException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public FemmeFulltextException(String message, Integer status, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	public FemmeFulltextException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FemmeFulltextException(String message, Integer status) {
		super(message);
		this.status = status;
	}
	
	public FemmeFulltextException(String message) {
		super(message);
	}
	
	public FemmeFulltextException(Throwable cause) {
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
