package gr.cite.femme.geo.core;

import java.time.Instant;
import java.util.Map;

public class CoverageGeo {
	private String id;
	
	private String coverageId;
	
	private Instant created;
	
	private Instant modified;
	
	private Map<String, String> geo;
	
	private String serverId;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCoverageId() {
		return coverageId;
	}
	
	public void setCoverageId(String coverageId) {
		this.coverageId = coverageId;
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
	
	public Map<String, String> getGeo() {
		return geo;
	}
	
	public void setGeo(Map<String, String> geo) {
		this.geo = geo;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
