package gr.cite.femme.metadatastore.mongodb;

import gr.cite.femme.model.Metadatum;

import java.time.Instant;

public class MetadataGridFSFile {

	private String id;
	private String filename;
	private Long length;
	private Integer chunkSize;
	private Instant uploadDate;
	private String md5;
	private Metadatum metadata;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public Integer getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(Integer chunkSize) {
		this.chunkSize = chunkSize;
	}

	public Instant getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Instant uploadDate) {
		this.uploadDate = uploadDate;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public Metadatum getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadatum metadata) {
		this.metadata = metadata;
	}
}
