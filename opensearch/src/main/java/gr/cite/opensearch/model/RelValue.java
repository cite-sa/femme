package gr.cite.opensearch.model;

public enum RelValue {
	RESULTS("results"),
	SUGGESTIONS("suggestions"),
	SELF("self"),
	COLLECTIONS("collection");

	private final String relValue;

	private RelValue(final String relValue) {
		this.relValue = relValue;
	}

	public String getRelValue() {
		return this.relValue;
	}

}
