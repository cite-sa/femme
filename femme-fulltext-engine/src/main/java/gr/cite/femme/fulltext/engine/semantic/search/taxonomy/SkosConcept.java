package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		SkosConcept that = (SkosConcept) o;
		
		if (! getUri().equals(that.getUri())) return false;
		if (getConceptScheme() != null ? ! getConceptScheme().equals(that.getConceptScheme()) : that.getConceptScheme() != null)
			return false;
		if (getLabel() != null ? ! getLabel().equals(that.getLabel()) : that.getLabel() != null) return false;
		if (! getPrefLabel().equals(that.getPrefLabel())) return false;
		return getAltLabel() != null ? getAltLabel().equals(that.getAltLabel()) : that.getAltLabel() == null;
	}
	
	@Override
	public int hashCode() {
		int result = getUri().hashCode();
		result = 31 * result + (getConceptScheme() != null ? getConceptScheme().hashCode() : 0);
		result = 31 * result + (getLabel() != null ? getLabel().hashCode() : 0);
		result = 31 * result + getPrefLabel().hashCode();
		result = 31 * result + (getAltLabel() != null ? getAltLabel().hashCode() : 0);
		return result;
	}
	
	@Override
	public String toString() {
		return this.prefLabel.stream().collect(Collectors.joining(","));
	}
}
