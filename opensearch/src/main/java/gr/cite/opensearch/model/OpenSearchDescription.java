package gr.cite.opensearch.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"shortName",
		"description",
		"url",
		"contact",
		"tags",
		"longName",
		"image",
		"query",
		"developer",
		"attribution",
		"syndicationRight",
		"adultContent",
		"language",
		"inputEncoding",
		"outputEncoding"
})

@XmlRootElement(name = "OpenSearchDescription")
public class OpenSearchDescription {

	@XmlElement(name = "ShortName", required = true)
	private String shortName;
	@XmlElement(name = "Description", required = true)
	private String description;
	@XmlElement(name = "Url", required = true)
	private List<Url> url;
	@XmlElement(name = "Contact", required = false)
	private String contact;
	@XmlElement(name = "Tags", required = false)
	private String tags;
	@XmlElement(name = "LongName", required = false)
	private String longName;
	@XmlElement(name = "Image", required = false)
	private Image image;
	@XmlElement(name = "Query", required = false)
	private Query query;
	@XmlElement(name = "Developer", required = false)
	private String developer;
	@XmlElement(name = "Attribution", required = false)
	private String attribution;
	@XmlElement(name = "SyndicationRight", required = false, defaultValue = "open")
	private String syndicationRight;
	@XmlElement(name = "AdultContent", required = false, defaultValue = "false")
	private String adultContent;
	@XmlElement(name = "language", required = false, defaultValue = "*")
	private String language;
	@XmlElement(name = "InputEncoding", required = false, defaultValue = "UTF-8")
	private String inputEncoding;
	@XmlElement(name = "OutputEncoding", required = false, defaultValue = "UTF-8")
	private String outputEncoding;

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Url> getUrl() {
		return url;
	}

	public void setUrl(List<Url> url) {
		this.url = url;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getSyndicationRight() {
		return syndicationRight;
	}

	public void setSyndicationRight(String syndicationRight) {
		this.syndicationRight = syndicationRight;
	}

	public String getAdultContent() {
		return adultContent;
	}

	public void setAdultContent(String adultContent) {
		this.adultContent = adultContent;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getInputEncoding() {
		return inputEncoding;
	}

	public void setInputEncoding(String inputEncoding) {
		this.inputEncoding = inputEncoding;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
}
