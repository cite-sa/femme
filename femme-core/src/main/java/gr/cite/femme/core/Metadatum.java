package gr.cite.femme.core;

public class Metadatum {

	String id;
	
	String name;

	String value;

	String contentType;
	
	
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
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

}
