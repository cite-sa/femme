package gr.cite.exmms.core;

public class Metadatum {

	String id;

	String name;

	String value;

	String mediaType;
	
	
	public Metadatum() {
		
	}
	
	public Metadatum(String id, String name, String value, String mediaType) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.mediaType = mediaType;
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

	public String getMedialType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

}
