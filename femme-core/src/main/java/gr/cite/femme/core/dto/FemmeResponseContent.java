package gr.cite.femme.core.dto;

public class FemmeResponseContent<T> {
	
	private boolean status;
	
	private String message;
	
	private T entity;
	
	public FemmeResponseContent() {
		
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}
	
	
}
