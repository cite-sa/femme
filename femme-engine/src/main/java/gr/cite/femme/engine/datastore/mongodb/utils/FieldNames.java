package gr.cite.femme.engine.datastore.mongodb.utils;

public final class FieldNames {
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String ENDPOINT = "endpoint";
	public static final String METADATA = "metadata";
	public static final String SYSTEMIC_METADATA = "systemicMetadata";
	public static final String STATUS = "status";
	
	public static final String COLLECTIONS = "collections";
	public static final String DATA_ELEMENTS = "dataElements";
	
	public static final String DATA_ELEMENT_COLLECTION_ID = COLLECTIONS + "." + ID;
	public static final String DATA_ELEMENT_COLLECTION_ENDPOINT = COLLECTIONS + "." + ENDPOINT;
	public static final String DATA_ELEMENT_COLLECTION_NAME = COLLECTIONS + "." + NAME;
	
	
	public static final String METADATA_FILENAME = "filename";
	//public static final String METADATA_FILE_ID = "fileId";
	public static final String METADATA_ELEMENT_ID = "elementId";
	public static final String METADATA_ELEMENT_ID_EMBEDDED = FieldNames.DATA_ELEMENT_COLLECTION_ENDPOINT + "." + FieldNames.METADATA_ELEMENT_ID;
	public static final String CONTENT_TYPE = "contentType";
	public static final String CHECKSUM = "checksum";
	
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
}
