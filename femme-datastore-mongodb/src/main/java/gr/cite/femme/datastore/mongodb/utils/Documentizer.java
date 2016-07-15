package gr.cite.femme.datastore.mongodb.utils;

import org.bson.Document;
import org.bson.types.ObjectId;

import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;

public class Documentizer {
	private static final String ELEMENT_ID_KEY = "_id";
	private static final String ELEMENT_NAME_KEY = "name";
	private static final String ELEMENT_ENDPOINT_KEY = "endpoint";
	private static final String ELEMENT_METADATA_KEY = "metadata";
	private static final String ELEMENT_SYSTEMIC_METADATA_KEY = "systemicMetadata";
	
	private static final String DATA_ELEMENT_DATA_ELEMENT_KEY = "dataElement";
	private static final String DATA_ELEMENT_COLLECTIONS_KEY = "collections";
	
	private static final String COLLECTION_DATA_ELEMENTS_KEY = "dataElements";
	/*private static final String COLLECTION_IS_COLLECTION_KEY = "isCollection";*/
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	
	public static Document toDocument(Element element) {
		Document document = new Document();
		if (element.getId() != null) {
			document.append(ELEMENT_ID_KEY, new ObjectId(element.getId()));
		} else {
			document.append(ELEMENT_ID_KEY, new ObjectId());
		}
		if (element.getEndpoint() != null) {
			document.append(ELEMENT_ENDPOINT_KEY, element.getEndpoint());
		}
		if (element.getName() != null) {
			document.append(ELEMENT_NAME_KEY, element.getName());
		}
		return document;
	}
	
	public static Document toOnlyIdDocument(Element element) {
		Document document = new Document();
		if (element.getId() != null) {
			document.append(ELEMENT_ID_KEY, new ObjectId(element.getId()));
		} else {
			document.append(ELEMENT_ID_KEY, new ObjectId());
		}
		return document;
	}
	
	public static Document toDocument(Metadatum metadatum) {
		Document document = new Document();
		if (metadatum.getId() != null) {
			document.append(METADATUM_ID_KEY, new ObjectId(metadatum.getId()));
		} else {
			document.append(METADATUM_ID_KEY, new ObjectId());
		}
		if (metadatum.getName() != null) {
			document.append(METADATUM_NAME_KEY, metadatum.getName());
		}
		/*if (metadatum.getName() != null) {
			document.append(ELEMENT_NAME_KEY, metadatum.getName());
		}*/
		return document;
	}
}
