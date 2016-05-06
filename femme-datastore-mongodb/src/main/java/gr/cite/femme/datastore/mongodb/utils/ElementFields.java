package gr.cite.femme.datastore.mongodb.utils;

public class ElementFields {
	private static final String ELEMENT_ID_KEY = "_id";
	private static final String ELEMENT_NAME_KEY = "name";
	private static final String ELEMENT_ENDPOINT_KEY = "endpoint";
	private static final String ELEMENT_METADATA_KEY = "metadata";
	private static final String ELEMENT_SYSTEMIC_METADATA_KEY = "systemicMetadata";
	
	public static String id() {
		return ELEMENT_ID_KEY;
	}
	
	public static String name() {
		return ELEMENT_NAME_KEY;
	}
	
	public static String endpoint() {
		return ELEMENT_ENDPOINT_KEY;
	}
	
	public static String metadata() {
		return ELEMENT_METADATA_KEY;
	}
	
	public static String systemicMetadata() {
		return ELEMENT_SYSTEMIC_METADATA_KEY;
	}
}
