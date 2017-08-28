package gr.cite.femme.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class FemmeResponseEntity<T> {
	
	@JsonProperty
	private String href;
	
	@JsonProperty
	private T body;
	
	public FemmeResponseEntity() {
		
	}

	public FemmeResponseEntity(T body) {
		this.body = body;
	}

	public FemmeResponseEntity(String href, T body) {
		this.href = href;
		this.body = body;
	}

	public String getHref() {
		return href;
	}

	public FemmeResponseEntity<T> setHref(String href) {
		this.href = href;
		return this;
	}

	public T getBody() {
		return body;
	}

	public FemmeResponseEntity<T> setBody(T body) {
		this.body = body;
		return this;
	}
	
	/*public static <T> Builder<T> builder() {
		return new Builder<T>();
	}*/
	
	/*public static class Builder<T> {
		
		private FemmeResponseEntity<T> entity;
		
		private Builder() {
			this.entity = new FemmeResponseEntity<T>();
		}
		
		public Builder<T> href(String href) {
			this.entity.setHref(href);
			return this;
		}
		
		public Builder<T> body(T body) {
			this.entity.setBody(body);
			return this;
		}
		
		public FemmeResponseEntity<T> execute() {
			return this.entity;
		}
	}*/
}
