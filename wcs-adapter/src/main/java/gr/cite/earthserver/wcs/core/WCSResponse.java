package gr.cite.earthserver.wcs.core;

import javax.ws.rs.core.MediaType;

public class WCSResponse {
	
	private String endpoint;
	
	private MediaType contentType;
	
	private String response;
	
	public WCSResponse() {
		
	}
	
	public WCSResponse(String endpoint, MediaType contentType, String response) {
		this.endpoint = endpoint;
		this.contentType = contentType;
		this.response = response;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public MediaType getContentType() {
		return contentType;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
