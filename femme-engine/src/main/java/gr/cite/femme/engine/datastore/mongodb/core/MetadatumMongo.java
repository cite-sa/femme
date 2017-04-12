package gr.cite.femme.engine.datastore.mongodb.core;

import gr.cite.femme.core.model.Metadatum;

public class MetadatumMongo extends Metadatum {
	
	private String importId;
	
	public MetadatumMongo() {
		
	}
	
	public MetadatumMongo(Metadatum metadatum, String importId, int status) {
		setId(metadatum.getId());
		setName(metadatum.getName());
		setElementId(metadatum.getElementId());
		setContentType(metadatum.getContentType());
		setValue(metadatum.getValue());
		
		this.importId = importId;
	}

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}
	
}
