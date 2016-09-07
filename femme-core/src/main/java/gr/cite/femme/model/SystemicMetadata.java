package gr.cite.femme.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SystemicMetadata {
	
	private String id;
	
	private DateTime created;
	
	private DateTime modified;
	
	private Map<String, MetadataStatistics> xPathFrequencies;
	

	public SystemicMetadata() {
		
	}
	
	public SystemicMetadata(String id, DateTime created, DateTime modified) {
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
	public DateTime getCreated() {
		return created;
	}
	public void setCreated(DateTime created) {
		this.created = created;
	}
	public DateTime getModified() {
		return modified;
	}
	public void setModified(DateTime modified) {
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