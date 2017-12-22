package gr.cite.earthserver.wcs.geo;

public class Axis {
	private String label;
	private String crs;
	private Double lowerCorner;
	private Double upperCorner;

	public Axis(String label, String crs, Double lowerCorner, Double upperCorner) {
		this.label = label;
		this.crs = crs;
		this.lowerCorner = lowerCorner;
		this.upperCorner = upperCorner;
	}

	public Axis(String label, String crs, String lowerCorner, String upperCorner) {
		this.label = label;
		this.crs = crs;
		this.lowerCorner = Double.parseDouble(lowerCorner);
		this.upperCorner = Double.parseDouble(upperCorner);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public Double getLowerCorner() {
		return lowerCorner;
	}

	public void setLowerCorner(Double lowerCorner) {
		this.lowerCorner = lowerCorner;
	}

	public void setLowerCorner(String lowerCorner) {
		this.lowerCorner = Double.parseDouble(lowerCorner);
	}

	public Double getUpperCorner() {
		return upperCorner;
	}

	public void setUpperCorner(Double upperCorner) {
		this.upperCorner = upperCorner;
	}

	public void setUpperCorner(String upperCorner) {
		this.upperCorner = Double.parseDouble(upperCorner);
	}

	@Override
	public String toString() {
		return "Axis{" +
				"label='" + label + '\'' +
				", crs='" + crs + '\'' +
				", lowerCorner='" + lowerCorner + '\'' +
				", upperCorner='" + upperCorner + '\'' +
				'}';
	}
}
