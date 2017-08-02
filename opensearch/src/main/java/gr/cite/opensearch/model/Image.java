package gr.cite.opensearch.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlValue;
import java.net.URI;

@XmlAccessorType(XmlAccessType.FIELD)
public class Image {

	@XmlAttribute(name = "height", required = false)
	private Integer height;

	@XmlAttribute(name = "width", required = false)
	private Integer width;

	@XmlAttribute(name = "type", required = false)
	private String type;

	@XmlValue
	private URI value;

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public URI getValue() {
		return value;
	}

	public void setValue(URI value) {
		this.value = value;
	}
}
