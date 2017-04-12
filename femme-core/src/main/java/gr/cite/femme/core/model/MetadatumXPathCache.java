package gr.cite.femme.core.model;

import java.util.ArrayList;
import java.util.List;

public class MetadatumXPathCache {
	
	private String id;
	
	private String xPath;
	
	private List<String> values;
	
	public MetadatumXPathCache() {
		values = new ArrayList<>();
	}
	
	public MetadatumXPathCache(String id, String xPath, List<String> values) {
		this.values = values;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getXPath() {
		return xPath;
	}

	public void setXPath(String xPath) {
		this.xPath = xPath;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
}
