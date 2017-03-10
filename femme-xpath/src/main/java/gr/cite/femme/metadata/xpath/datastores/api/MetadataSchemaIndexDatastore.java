package gr.cite.femme.metadata.xpath.datastores.api;

import gr.cite.femme.metadata.xpath.core.MetadataSchema;

import java.util.List;

public interface MetadataSchemaIndexDatastore {

	public void close();

	public void indexSchema(MetadataSchema metadataSchema);

	public List<MetadataSchema> findMetadataIndexPath(String regex);

	public List<MetadataSchema> findArrayMetadataIndexPaths();

	public List<MetadataSchema> findArrayMetadataIndexPaths(String id);

}
