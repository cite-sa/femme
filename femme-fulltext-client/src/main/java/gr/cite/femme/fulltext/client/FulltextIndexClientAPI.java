package gr.cite.femme.fulltext.client;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.core.FulltextSearchQueryMessenger;

import java.util.List;
import java.util.Map;

public interface FulltextIndexClientAPI {
	void insert(String elementId, String metadatumId, Map<String, Object> fields);
	void delete(String id);
	void deleteByElementId(String elementId);
	void deleteByMetadatumId(String metadatumId);
	List<FulltextDocument> search(FulltextSearchQueryMessenger query);
}
