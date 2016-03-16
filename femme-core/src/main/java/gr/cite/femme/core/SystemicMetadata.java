package gr.cite.femme.core;

import java.time.Instant;

public class SystemicMetadata {
	
	private String id;
	
	private Instant created;
	
	private Instant modified;

	public SystemicMetadata() {
		created = Instant.now();
		modified = Instant.now();
	}
	
	public SystemicMetadata(String id, Instant created, Instant modified) {
		this.id = id;
		this.created = created;
		this.modified = modified;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
