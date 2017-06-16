package gr.cite.earthserver.wcs.geo;

public class Axis {
	private String crs;
	private String label;

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Axis(String crs, String label) {
		this.crs = crs;
		this.label = label;
	}
}
