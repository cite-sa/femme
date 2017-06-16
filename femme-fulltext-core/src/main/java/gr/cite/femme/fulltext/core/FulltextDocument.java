package gr.cite.femme.fulltext.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FulltextDocument {
	private String id;
	private String elementId;
	private String metadatumId;
	private Map<String, Object> fulltextFields = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getMetadatumId() {
		return metadatumId;
	}

	public void setMetadatumId(String metadatumId) {
		this.metadatumId = metadatumId;
	}

	public Object getFulltextField(String name) {
		return this.fulltextFields.get(name);
	}

	public void setFulltextFields(Map<String, Object> fulltextFields) {
		this.fulltextFields = fulltextFields;
	}

	@JsonAnyGetter
	public Map<String, Object> getFulltextFields() {
		return this.fulltextFields;
	}

	@JsonAnySetter
	public void setFulltextField(String name, Object value) {
		this.fulltextFields.put(name, value);
	}
}
