package gr.cite.femme.fulltext.client;

import gr.cite.femme.fulltext.core.FulltextDocument;

import java.util.Map;

public interface FulltextSearchClientAPI {
	void insert(String elementId, String metadatumId, Map<String, Object> fields);
	void delete(String id);
	void deleteByElementId(String elementId);
	void search();
}
