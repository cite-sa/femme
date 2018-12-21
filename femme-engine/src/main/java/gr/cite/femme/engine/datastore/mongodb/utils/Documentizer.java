package gr.cite.femme.engine.datastore.mongodb.utils;

import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.core.model.Metadatum;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Documentizer {
	
	public static Document toDocument(Element element) {
		Document document = new Document();
		if (element.getId() != null) {
			document.append(FieldNames.ID, new ObjectId(element.getId()));
		}
		if (element.getEndpoint() != null) {
			document.append(FieldNames.ENDPOINT, element.getEndpoint());
		}
		if (element.getName() != null) {
			document.append(FieldNames.NAME, element.getName());
		}
		if (element.getSystemicMetadata() != null) {
			if (element.getSystemicMetadata().getCreated() != null) {
				document.append(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.CREATED, element.getSystemicMetadata().getCreated().toEpochMilli());
			}
			if (element.getSystemicMetadata().getModified() != null) {
				document.append(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED, element.getSystemicMetadata().getModified().toEpochMilli());
			}
			if (element.getSystemicMetadata().getStatus() != null) {
				document.append(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, element.getSystemicMetadata().getStatus().getStatusCode());
			}
		}
		return document;
	}
	
	public static Document toOnlyIdDocument(Element element) {
		Document document = new Document();
		if (element.getId() != null) {
			document.append(FieldNames.ID, new ObjectId(element.getId()));
		} else {
			document.append(FieldNames.ID, new ObjectId());
		}
		return document;
	}
	
	public static Document toIdNameDocument(Element element) {
		Document document = new Document();
		if (element.getId() != null) {
			document.append(FieldNames.ID, new ObjectId(element.getId()));
		} else {
			document.append(FieldNames.ID, new ObjectId());
		}
		if (element.getName() != null) {
			document.append(FieldNames.NAME, element.getName());
		}
		return document;
	}
	
	public static Document toDocument(Metadatum metadatum) {
		Document document = new Document();
		if (metadatum.getId() != null) {
			document.append(FieldNames.ID, new ObjectId(metadatum.getId()));
		} else {
			document.append(FieldNames.ID, new ObjectId());
		}
		if (metadatum.getName() != null) {
			document.append(FieldNames.NAME, metadatum.getName());
		}
		/*if (metadatum.getName() != null) {
			document.append(ELEMENT_NAME_KEY, metadatum.getName());
		}*/
		return document;
	}
}
