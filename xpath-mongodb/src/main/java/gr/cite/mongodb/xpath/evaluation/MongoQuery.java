package gr.cite.mongodb.xpath.evaluation;

import org.bson.Document;

public class MongoQuery {
	
	private Document document;
	
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
	
	/*public String build() {
		return queryBuilder.toString();
	}*/

}
