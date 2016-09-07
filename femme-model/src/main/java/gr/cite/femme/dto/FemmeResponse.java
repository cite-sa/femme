package gr.cite.femme.dto;

public class FemmeResponse<T> {
	
	private boolean status;
	
	private String message;
	
	private T entity;

	public boolean getStatus() {
		return status;
	}

	public FemmeResponse<T> setStatus(boolean status) {
		this.status = status;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public FemmeResponse<T> setMessage(String message) {
		this.message = message;
		return this;
	}

	public T getEntity() {
		return entity;
	}

	public FemmeResponse<T> setEntity(T entity) {
		this.entity = entity;
		return this;
	}
	
	
}
