package gr.cite.pipelinenew.geojson;

import org.geojson.GeoJsonObject;

public class GeoJsonInfo {
	private String crs;
	private GeoJsonObject geo;
	
	public String getCrs() {
		return crs;
	}
	
	public void setCrs(String crs) {
		this.crs = crs;
	}
	
	public GeoJsonObject getGeo() {
		return geo;
	}
	
	public void setGeo(GeoJsonObject geo) {
		this.geo = geo;
	}
}
