package gr.cite.femme.core.geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.GeoJsonObject;

import java.time.Instant;

public class CoverageGeo {

	@JsonProperty("_id")
	private String id;

	@JsonProperty("coverageId")
	private String coverageId;

	@JsonProperty("created")
	private Instant created;

	@JsonProperty("modified")
	private Instant modified;

	@JsonProperty("geometry")
	private GeoJsonObject geo;

	@JsonProperty("serverId")
	private String serverId;

	@JsonProperty("dataElementId")
	private String dataElementId;

	@JsonProperty("crs")
	private String crs;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCoverageId() {
		return coverageId;
	}
	
	public void setCoverageId(String coverageId) {
		this.coverageId = coverageId;
	}
	
	public Instant getCreated() {
		return created;
	}
	
	public void setCreated(Instant created) {
		this.created = created;
	}
	
	public Instant getModified() {
		return modified;
	}
	
	public void setModified(Instant modified) {
		this.modified = modified;
	}
	
	public GeoJsonObject  getGeo() {
		return geo;
	}
	
	public void setGeo(GeoJsonObject  geo) {
		this.geo = geo;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getDataElementId() {
		return dataElementId;
	}

	public void setDataElementId(String dataElementId) {
		this.dataElementId = dataElementId;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}
}
