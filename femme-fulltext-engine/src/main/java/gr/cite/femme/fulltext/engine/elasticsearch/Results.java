package gr.cite.femme.fulltext.engine.elasticsearch;

import gr.cite.femme.fulltext.core.FulltextDocument;

import java.util.ArrayList;
import java.util.List;

public class Results {
	private String uniqueTerm;
	private List<FulltextDocument> documents = new ArrayList<>();
	
	public Results(String uniqueTerm, List<FulltextDocument> documents) {
		this.uniqueTerm = uniqueTerm;
		this.documents = documents;
	}
	
	public String getUniqueTerm() {
		return uniqueTerm;
	}
	
	public void setUniqueTerm(String uniqueTerm) {
		this.uniqueTerm = uniqueTerm;
	}
	
	public List<FulltextDocument> getDocuments() {
		return documents;
	}
	
	public void setDocuments(List<FulltextDocument> documents) {
		this.documents = documents;
	}
}
