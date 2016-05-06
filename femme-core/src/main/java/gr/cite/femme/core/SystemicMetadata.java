package gr.cite.femme.core;

import java.time.Instant;
import java.util.Map;

public class SystemicMetadata {
	
	private String id;
	
	private Instant created;
	
	private Instant modified;
	
	private Map<String, MetadataStatistics> xPathFrequencies;

	public SystemicMetadata() {
		
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

	public Map<String, MetadataStatistics> getxPathFrequencies() {
		return xPathFrequencies;
	}

	public void setXPathFrequencies(Map<String, MetadataStatistics> xPathFrequencies) {
		this.xPathFrequencies = xPathFrequencies;
	}
	public void updateXPathFrequencies(String xPath) {
		if (xPath != null && !xPath.equals("")) {
			if (!xPathFrequencies.containsKey(xPath)) {
				xPathFrequencies.put(xPath, new MetadataStatistics());
			} else {
				xPathFrequencies.get(xPath).updateStatictics();
			}
		}
	}
}
