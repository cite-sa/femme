package gr.cite.femme.fulltext.core;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FulltextSearchQueryMessenger {
	private String elementId;
	private String metadatumId;
	private String collectionId;
	private String elementName;
	private String collectionName;
	private FulltextField metadataField;
	private FulltextField autocompleteField;
	private String allField;
	private String expand;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getMetadatumId() {
		return metadatumId;
	}

	public void setMetadatumId(String metadatumId) {
		this.metadatumId = metadatumId;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public FulltextField getMetadataField() {
		return metadataField;
	}

	public void setMetadataField(FulltextField metadataField) {
		this.metadataField = metadataField;
	}

	public FulltextField getAutocompleteField() {
		return autocompleteField;
	}

	public void setAutocompleteField(FulltextField autocompleteField) {
		this.autocompleteField = autocompleteField;
	}

	public String getAllField() {
		return allField;
	}

	public void setAllField(String allField) {
		this.allField = allField;
	}
	
	public String getExpand() {
		return expand;
	}
	
	public void setExpand(String expand) {
		this.expand = expand;
	}
}
