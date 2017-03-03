package gr.cite.femme.metadata.xpath.datastores;

import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.exceptions.MetadataIndexException;

import java.io.IOException;
import java.util.List;

public interface MetadataIndexDatastore {

	public void close() throws IOException;

	public void indexMetadatum(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException;

	public List<IndexableMetadatum> query(String query) throws MetadataIndexException;

}
