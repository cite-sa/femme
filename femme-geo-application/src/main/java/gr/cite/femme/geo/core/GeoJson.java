package gr.cite.femme.geo.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeoJson {
	@JsonProperty("type")
	private String type;
	@JsonProperty("coordinates")
	private List<List<List<Double>>> coordinates;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<List<List<Double>>> getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(List<List<List<Double>>> coordinates) {
		this.coordinates = coordinates;
	}
}
