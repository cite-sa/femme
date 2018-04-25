package gr.cite.femme.core.geo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.geojson.GeoJsonObject;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CoverageGeo {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("coverageName")
	private String coverageName;
	
	@JsonProperty("created")
	@JsonSerialize(using = InstantSerializer.class)
	private Instant created;
	
	@JsonProperty("modified")
	@JsonSerialize(using = InstantSerializer.class)
	private Instant modified;
	
	@JsonProperty("geometry")
	private GeoJsonObject geo;
	
	@JsonProperty("serverId")
	private String serverId;
	
	@JsonProperty("dataElementId")
	private String dataElementId;
	
	@JsonProperty("crs")
	private String crs;
	
	@JsonProperty("serverName")
	private String serverName;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCoverageName() {
		return coverageName;
	}
	
	public void setCoverageName(String coverageName) {
		this.coverageName = coverageName;
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
	
	public GeoJsonObject getGeo() {
		return geo;
	}
	
	public void setGeo(GeoJsonObject geo) {
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
	
	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
