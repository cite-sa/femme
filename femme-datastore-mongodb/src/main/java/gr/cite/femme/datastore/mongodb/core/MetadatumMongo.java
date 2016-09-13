package gr.cite.femme.datastore.mongodb.core;

import gr.cite.femme.model.Metadatum;

public class MetadatumMongo extends Metadatum {
	
	private String importId;
	
	private int status;
	
	public MetadatumMongo() {
		
	}
	
	public MetadatumMongo(Metadatum metadatum, String importId, int status) {
		setId(metadatum.getId());
		setName(metadatum.getName());
		setElementId(metadatum.getElementId());
		setContentType(metadatum.getContentType());
		setValue(metadatum.getValue());
		
		this.importId = importId;
		this.status = status;
	}

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
