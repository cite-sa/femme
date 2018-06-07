package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TaxonomyTerm {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("label")
	private List<String> label;
	
	@JsonProperty("broader")
	private List<String> broader = new ArrayList<>();
	
	@JsonProperty("narrower")
	private List<String> narrower = new ArrayList<>();
	
	@JsonProperty("related")
	private List<String> related = new ArrayList<>();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<String> getLabel() {
		return label;
	}
	
	public void setLabel(List<String> label) {
		this.label = label;
	}
	
	public List<String> getBroader() {
		return broader;
	}
	
	public void setBroader(List<String> broader) {
		this.broader = broader;
	}
	
	public List<String> getNarrower() {
		return narrower;
	}
	
	public void setNarrower(List<String> narrower) {
		this.narrower = narrower;
	}
	
	public List<String> getRelated() {
		return related;
	}
	
	public void setRelated(List<String> related) {
		this.related = related;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		TaxonomyTerm that = (TaxonomyTerm) o;
		
		return getId().equals(that.getId());
	}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
