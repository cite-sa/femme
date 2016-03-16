package gr.cite.femme.core;

public class Metadatum {

	private String id;
	
	private String elementId;
	
	private String name;

	private String value;
	
	private String contentType;
	
	public Metadatum() {
	}
	
	public Metadatum(String name, String value, String contentType) {
		this.name = name;
		this.value = value;
		this.contentType = contentType;
	}
	public Metadatum(String id, String name, String value, String contentType) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.contentType = contentType;
	}
	public Metadatum(String id, String elementId, String name, String value, String contentType) {
		this.id = id;
		this.elementId = elementId;
		this.name = name;
		this.value = value;
		this.contentType = contentType;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	@Override
	public String toString() {
		StringBuilder metadataBuilder = new StringBuilder();
		metadataBuilder.append("\t" + this.id);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.name);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.contentType);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.value);
		metadataBuilder.append("\n");
		
		return metadataBuilder.toString();
	}
}
