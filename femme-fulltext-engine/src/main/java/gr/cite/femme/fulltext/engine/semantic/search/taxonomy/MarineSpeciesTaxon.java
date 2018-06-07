package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MarineSpeciesTaxon {
	@JsonProperty("AphiaID")
	private String AphiaID;
	
	@JsonProperty("rank")
	private String rank;
	
	@JsonProperty("scientificname")
	private String scientificname;
	
	@JsonProperty("child")
	private MarineSpeciesTaxon child;
	
	public String getAphiaID() {
		return AphiaID;
	}
	
	public void setAphiaID(String aphiaID) {
		AphiaID = aphiaID;
	}
	
	public String getRank() {
		return rank;
	}
	
	public void setRank(String rank) {
		this.rank = rank;
	}
	
	public String getScientificname() {
		return scientificname;
	}
	
	public void setScientificname(String scientificname) {
		this.scientificname = scientificname;
	}
	
	public MarineSpeciesTaxon getChild() {
		return child;
	}
	
	public void setChild(MarineSpeciesTaxon child) {
		this.child = child;
	}
}
