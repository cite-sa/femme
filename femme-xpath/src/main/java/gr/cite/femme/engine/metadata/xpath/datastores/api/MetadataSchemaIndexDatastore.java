package gr.cite.femme.engine.metadata.xpath.datastores.api;

import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

public interface MetadataSchemaIndexDatastore {

	public void close();

	public void index(MetadataSchema metadataSchema);

	public List<MetadataSchema> findMetadataIndexPath(String regex);

	public Map<String, List<String>> findMetadataIndexPathByRegexAndGroupById(String regex);

	public List<MetadataSchema> findArrayMetadataIndexPaths();

	public List<MetadataSchema> findArrayMetadataIndexPathsByRegex(String regex);

	public List<MetadataSchema> findArrayMetadataIndexPaths(String id);

}
