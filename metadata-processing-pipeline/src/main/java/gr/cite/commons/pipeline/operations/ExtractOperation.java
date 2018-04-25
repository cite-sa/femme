package gr.cite.commons.pipeline.operations;

public class ExtractOperation extends ProcessingPipelineOperation {
	private Format format;
	private String query;
	
	public Format getFormat() {
		return format;
	}
	
	public void setFormat(Format format) {
		this.format = format;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
}
