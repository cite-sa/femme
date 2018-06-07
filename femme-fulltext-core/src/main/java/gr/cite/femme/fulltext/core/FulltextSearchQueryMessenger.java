package gr.cite.femme.fulltext.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FulltextSearchQueryMessenger {
	
	@JsonProperty("elementId")
	private String elementId;
	
	@JsonProperty("metadatumId")
	private String metadatumId;
	
	@JsonProperty("collectionId")
	private String collectionId;
	
	@JsonProperty("elementName")
	private String elementName;
	
	@JsonProperty("collectionName")
	private String collectionName;
	
	@JsonProperty("metadataField")
	private FulltextField metadataField;
	
	@JsonProperty("autocompleteField")
	private FulltextField autocompleteField;
	
	@JsonProperty("allField")
	private String allField;
	
	@JsonProperty("expand")
	private ExpansionQuery expand;

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
	
	public ExpansionQuery getExpand() {
		return expand;
	}
	
	public void setExpand(ExpansionQuery expand) {
		this.expand = expand;
	}
}
