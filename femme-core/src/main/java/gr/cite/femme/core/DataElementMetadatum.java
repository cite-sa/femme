package gr.cite.femme.core;

public class DataElementMetadatum extends Metadatum {
	
	public DataElementMetadatum() {
		super();
	}
	
	public DataElementMetadatum(String name, String value, String mediaType) {
		super(name, value, mediaType);
	}
	
	public DataElementMetadatum(String id, String name, String value, String mediaType) {
		super(id, name, value, mediaType);
	}
}
