package gr.cite.opensearch.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.net.URI;
import java.net.URL;

@XmlAccessorType(XmlAccessType.FIELD)
public class Url {
	@XmlAttribute(name = "template", required = true)
	private URI template;
	@XmlAttribute(name = "type", required = true)
	private String type;
	@XmlAttribute(name = "rel", required = false)
	private RelValue rel = RelValue.RESULTS;
	@XmlAttribute(name = "indexOffset", required = false)
	private Integer indexOffset = 1;
	@XmlAttribute(name = "indexOffset", required = false)
	private Integer pageOffset = 1;

	public URI getTemplate() {
		return template;
	}

	public void setTemplate(URI template) {
		this.template = template;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RelValue getRel() {
		return rel;
	}

	public void setRel(RelValue rel) {
		this.rel = rel;
	}

	public Integer getIndexOffset() {
		return indexOffset;
	}

	public void setIndexOffset(Integer indexOffset) {
		this.indexOffset = indexOffset;
	}

	public Integer getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(Integer pageOffset) {
		this.pageOffset = pageOffset;
	}
}
