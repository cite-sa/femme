package gr.cite.femme.semantic.search.taxonomy;

import java.net.URI;
import java.util.List;

public class SkosConcept {
	private URI uri;
	private URI conceptScheme;
	private List<String> label;
	private List<String> prefLabel;
	private List<String> altLabel;
	private List<URI> broader;
	private List<URI> narrower;
	private List<URI> related;
	
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public URI getConceptScheme() {
		return conceptScheme;
	}
	
	public void setConceptScheme(URI conceptScheme) {
		this.conceptScheme = conceptScheme;
	}
	
	public List<String> getLabel() {
		return label;
	}
	
	public void setLabel(List<String> label) {
		this.label = label;
	}
	
	public List<String> getPrefLabel() {
		return prefLabel;
	}
	
	public void setPrefLabel(List<String> prefLabel) {
		this.prefLabel = prefLabel;
	}
	
	public List<String> getAltLabel() {
		return altLabel;
	}
	
	public void setAltLabel(List<String> altLabel) {
		this.altLabel = altLabel;
	}
	
	public List<URI> getBroader() {
		return broader;
	}
	
	public void setBroader(List<URI> broader) {
		this.broader = broader;
	}
	
	public List<URI> getNarrower() {
		return narrower;
	}
	
	public void setNarrower(List<URI> narrower) {
		this.narrower = narrower;
	}
	
	public List<URI> getRelated() {
		return related;
	}
	
	public void setRelated(List<URI> related) {
		this.related = related;
	}
}
