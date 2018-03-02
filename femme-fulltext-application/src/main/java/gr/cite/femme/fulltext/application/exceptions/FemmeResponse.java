package gr.cite.femme.fulltext.application.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class FemmeResponse<T> {
	
	@JsonProperty
	private Integer status;
	
	@JsonProperty
	private Integer code;
	
	@JsonProperty
	private String message;
	
	@JsonProperty
	private String developerMessage;
	
	@JsonProperty
	private FemmeResponseEntity<T> entity;

	public FemmeResponse() {
		
	}
	
	public FemmeResponse(Integer status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public FemmeResponse(Integer status, String message, String developerMessage) {
		this.status = status;
		this.message = message;
		this.developerMessage = developerMessage;
	}

	public FemmeResponse(Integer status, Integer code, String message, String developerMessage, FemmeResponseEntity<T> entity) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.developerMessage = developerMessage;
		this.entity = entity;
	}
	
	public Integer getStatus() {
		return status;
	}

	public FemmeResponse<T> setStatus(Integer status) {
		this.status = status;
		return this;
	}

	public Integer getCode() {
		return this.code;
	}

	public FemmeResponse<T> setCode(Integer code) {
		this.code = code;
		return this;
	}
	
	public String getMessage() {
		return this.message;
	}

	public FemmeResponse<T> setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public FemmeResponseEntity<T> getEntity() {
		return entity;
	}

	public FemmeResponse<T> setEntity(FemmeResponseEntity<T> entity) {
		this.entity = entity;
		return this;
	}
	
	
}
