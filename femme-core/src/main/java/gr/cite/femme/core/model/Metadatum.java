package gr.cite.femme.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class Metadatum {

	@JsonProperty("id")
	private String id;

	@JsonProperty("elementId")
	private String elementId;

	@JsonProperty("endpoint")
	private String endpoint;

	@JsonProperty("name")
	private String name;

	@JsonInclude(Include.NON_NULL)
	@JsonProperty("value")
	private String value;

	@JsonProperty("contentType")
	private String contentType;

	@JsonProperty("systemicMetadata")
	private SystemicMetadata systemicMetadata;

	@JsonIgnore
	private String checksum;

	/*private List<MetadatumXPathCache> xPathCache;*/

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public SystemicMetadata getSystemicMetadata() {
		return systemicMetadata;
	}

	public void setSystemicMetadata(SystemicMetadata systemicMetadata) {
		this.systemicMetadata = systemicMetadata;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/*public List<MetadatumXPathCache> getXPathCache() {
		return xPathCache;
	}

	public void setXPathCache(List<MetadatumXPathCache> xPathCache) {
		this.xPathCache = xPathCache;
	}*/

	@Override
	public String toString() {
		StringBuilder metadataBuilder = new StringBuilder();
		metadataBuilder.append("\t" + this.id);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.name);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.contentType);
		metadataBuilder.append("\n");
		metadataBuilder.append("\t" + this.value);
		metadataBuilder.append("\n");
		
		return metadataBuilder.toString();
	}
}
