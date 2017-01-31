package gr.cite.femme.metadata.xpath.mongodb.evaluation;

import org.bson.Document;

public class MongoQuery {
	
	private Document document;

	private StringBuilder pathRegEx = new StringBuilder();

	
	public MongoQuery() {
		this.document = new Document();
	}

	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public void append(String key, Object value) {
		this.document.append(key, value);
	}

	public StringBuilder getPathRegEx() {
		return pathRegEx;
	}

	public void setPathRegEx(StringBuilder pathRegEx) {
		this.pathRegEx = pathRegEx;
	}

	public void appendPathRegEx(String pathRegExPart) {
		pathRegEx.append(pathRegExPart);
	}
}
