package gr.cite.femme.datastore.mongodb.utils;

import java.util.List;

import org.bson.Document;

import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;

public class DataElementQueryDocumentBuilder {
	private boolean and;
	
	private boolean or;
	
	private Element element;
	
	private List<Element> elements;
	
	private MetadataQueryDocumentBuilder metadata;
	
	public DataElementQueryDocumentBuilder(Element element) {
		this.element = element;
		and = false;
		or = false;
	}
	
	public DataElementQueryDocumentBuilder dataElement(Element element) {
		if (!and && !or) {
			this.element = element;
		} else if (and) {
			
		} else if (or) {
			
		}
		return this;
	}
	public DataElementQueryDocumentBuilder and() throws UnsupportedQueryOperationException {
		if (!and && !or) {
			and = true;
		} else if (or) {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public DataElementQueryDocumentBuilder or() throws UnsupportedQueryOperationException {
		if (!and && !or) {
			or = true;
		} else if (and) {
			throw new UnsupportedQueryOperationException("");
		}
		if (and) {
			elements.add(element);
		} else {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public DataElementQueryDocumentBuilder isParentOf(Element element) {
		return null;
	}
	public DataElementQueryDocumentBuilder hasMetadata(MetadataQueryDocumentBuilder metadataBuilder) throws UnsupportedQueryOperationException {
		this.metadata = metadataBuilder;
		return this;
	}
	public Document build() {
		Document doc = Documentizer.toDocument(element);
		doc.append("metadata", metadata.build());
		return doc;
	}
	
	
}
