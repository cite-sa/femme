package gr.cite.femme.fulltext.engine;

import gr.cite.femme.fulltext.core.FulltextDocument;

import java.util.List;

public class FulltextSemanticResultSemantic {
	private String term;
	private List<FulltextDocument> docs;
	
	public FulltextSemanticResultSemantic(String term, List<FulltextDocument> docs) {
		this.term = term;
		this.docs = docs;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public List<FulltextDocument> getDocs() {
		return docs;
	}
	
	public void setDocs(List<FulltextDocument> docs) {
		this.docs = docs;
	}
}
