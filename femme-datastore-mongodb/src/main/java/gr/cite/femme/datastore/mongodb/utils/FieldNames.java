package gr.cite.femme.datastore.mongodb.utils;

public class FieldNames {
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String ENDPOINT = "endpoint";
	public static final String METADATA = "metadata";
	public static final String SYSTEMIC_METADATA = "systemicMetadata";
	
	public static final String SUB_DATA_ELEMENTS = "subDataElements";
	public static final String COLLECTIONS = "collections";
	public static final String DATA_ELEMENT_COLLECTION_ID = COLLECTIONS + "." + ID;
	
	public static final String DATA_ELEMENTS = "dataElements";
	
	public static final String METADATA_ELEMENT_ID = "elementId";
	
	public static final String FILENAME = "fileName";
	public static final String FILE_ID = "fileId";
	public static final String ELEMENT_ID = "elementId";
	public static final String CONTENT_TYPE = "contentType";
	
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
}
