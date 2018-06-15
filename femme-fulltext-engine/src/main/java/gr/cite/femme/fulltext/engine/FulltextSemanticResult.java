package gr.cite.femme.fulltext.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.cite.femme.fulltext.core.FulltextDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FulltextSemanticResult {
	
	@JsonProperty("fulltext")
	private FulltextDocument fulltextResult;
	
	/*@JsonProperty("semantic")
	private List<FulltextDocument> semanticResults = new ArrayList<>();*/
	@JsonProperty("semantic")
	private List<List<FulltextDocument>> semanticResults = new ArrayList<>();
	
	public FulltextDocument getFulltextResult() {
		return fulltextResult;
	}
	
	public void setFulltextResult(FulltextDocument fulltextResult) {
		this.fulltextResult = fulltextResult;
	}
	
	public List<List<FulltextDocument>> getSemanticResults() {
		return semanticResults;
	}
	
	public void setSemanticResults(List<List<FulltextDocument>> semanticResults) {
		this.semanticResults = semanticResults;
	}
}
