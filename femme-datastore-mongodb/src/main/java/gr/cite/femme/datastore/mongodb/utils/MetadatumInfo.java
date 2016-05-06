package gr.cite.femme.datastore.mongodb.utils;

import gr.cite.femme.core.Metadatum;

public class MetadatumInfo {
	private String fileId;
	
	private String fileName;
	
	private Metadatum metadatum;
	
	public MetadatumInfo() {
		
	}
	
	public MetadatumInfo(String fileId, String fileName, Metadatum metadatum) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.metadatum = metadatum;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Metadatum getMetadatum() {
		return metadatum;
	}

	public void setMetadatum(Metadatum metadatum) {
		this.metadatum = metadatum;
	}
	
	
}
