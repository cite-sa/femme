package gr.cite.femme.datastore.mongodb.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import gr.cite.femme.core.Element;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;

public class RootQueryDocumentBuilder {
	private boolean and;
	
	private boolean or;
	
	Document root;
	
	private List<DataElementQueryDocumentBuilder> elements;
	
	public RootQueryDocumentBuilder() {
		elements = new ArrayList<>();
	}
	
	public RootQueryDocumentBuilder element(DataElementQueryDocumentBuilder element) throws UnsupportedQueryOperationException {
		if (and || or || elements.size() == 0) {
			elements.add(element);
		} else {
			throw new UnsupportedQueryOperationException();
		}
		return this;
	}
	public RootQueryDocumentBuilder and() throws UnsupportedQueryOperationException {
		if (!and && !or) {
			and = true;
		} else if (or) {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public RootQueryDocumentBuilder or() throws UnsupportedQueryOperationException {
		if (!and && !or) {
			or = true;
		} else if (and) {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public Document build() {
		Document doc;
		if (and) {
			List<Document> docs = elements.stream().map(element -> element.build()).collect(Collectors.toList());
			doc = new Document().append("$and", docs);
		} else if (or) {
			List<Document> docs = elements.stream().map(element -> element.build()).collect(Collectors.toList());
			doc = new Document().append("$or", docs);
		} else {
			doc = elements.get(0).build();
		}
		return doc;
	}
		
		

}
