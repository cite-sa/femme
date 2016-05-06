package gr.cite.femme.datastore.mongodb.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;

public class MetadataQueryDocumentBuilder {
	boolean and;
	
	boolean or;
	
	List<Metadatum> metadata;
	
	public MetadataQueryDocumentBuilder() {
		or = false;
		and = false;
		metadata = new ArrayList<>();
	}
	
	public MetadataQueryDocumentBuilder metadatum(Metadatum metadatum) throws UnsupportedQueryOperationException {
		if (and || or || metadata.size() == 0) {
			this.metadata.add(metadatum);
		} else {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public MetadataQueryDocumentBuilder and() throws UnsupportedQueryOperationException {
		if (!and && !or) {
			and = true;
		} else if (or){
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	public MetadataQueryDocumentBuilder or() throws UnsupportedQueryOperationException {
		if (!or && !and) {
			or = true;
		} else if (and) {
			throw new UnsupportedQueryOperationException("");
		}
		return this;
	}
	
	public Document build() {
		Document doc = null;
		if (or) {
			doc = new Document().append("$or",
					metadata.stream().map(metadatum -> Documentizer.toDocument(metadatum)).collect(Collectors.toList()));
			
		} else if (and) {
			doc = new Document().append("$and",
					metadata.stream().map(metadatum -> Documentizer.toDocument(metadatum)).collect(Collectors.toList()));
		} else if (metadata.size() == 1) {
			doc = Documentizer.toDocument(metadata.get(0));
		}
		return new Document().append("$elemMatch", doc);
	}
}
