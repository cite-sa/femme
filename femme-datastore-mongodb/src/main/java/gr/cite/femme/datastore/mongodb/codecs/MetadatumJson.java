package gr.cite.femme.datastore.mongodb.codecs;

import gr.cite.femme.core.Metadatum;

public class MetadatumJson extends Metadatum {
	public MetadatumJson() {
		
	}
	
	public MetadatumJson(String id, String elementId, String name, String value, String contentType) {
		super(id, elementId, name, value, contentType);
	}
	
	public MetadatumJson(Metadatum metadatum) {
		super(metadatum.getId(), metadatum.getElementId(), metadatum.getName(), metadatum.getValue(), metadatum.getContentType());
	}
}