package gr.cite.femme.core.geo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ServerGeo {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("collectionId")
	private String collectionId;
	
	@JsonProperty("serverName")
	private String serverName;
	
	@JsonProperty("created")
	private Instant created;
	
	@JsonProperty("modified")
	private Instant modified;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCollectionId() {
		return collectionId;
	}
	
	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public Instant getCreated() {
		return created;
	}
	
	public void setCreated(Instant created) {
		this.created = created;
	}
	
	public Instant getModified() {
		return modified;
	}
	
	public void setModified(Instant modified) {
		this.modified = modified;
	}
}
