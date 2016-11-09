/*package gr.cite.femme.dto;

public class FemmeResponseBuilder<T> {
	
	private T entity;
	
	private boolean status;
	
	private String message;
	
	private FemmeResponseBuilder() {
		
	}
	
	public static FemmeResponseBuilder<?> response() {
		return new FemmeResponseBuilder<>();
	}
	
	public <T> FemmeResponseBuilder<T> ok() {
		status = true;
		message = "ok";
		
		return null;
	}
	
	public FemmeResponseBuilder<?> error(String message) {
		this.status = false;
		this.message = message;
		
		return this;
	}
	
	public FemmeResponseBuilder<?> error(T entity) {
		this.entity = entity;
		return this;
	}
	
	public FemmeResponse<T> build() {
		FemmeResponse<T> femmeResponse = new FemmeResponse<>();
		femmeResponse.setStatus(status);
		femmeResponse.setMessage(message);
		femmeResponse.setEntity(entity);
		
		return femmeResponse;
		
	}
}
*/