package gr.cite.femme.index.api.client;

public class MetadatumIndex {

	private String id;
	
	private String elementId;
	
	private String contentType;

	private String value;
	
	
	public MetadatumIndex() {
		
	}
	
	public MetadatumIndex(String id, String elementId, String contentType, String value) {
		this.id = id;
		this.elementId = elementId;
		this.contentType = contentType;
		this.value = value;
	}
	
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
