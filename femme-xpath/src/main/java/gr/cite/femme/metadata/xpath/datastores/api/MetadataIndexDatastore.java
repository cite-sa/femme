package gr.cite.femme.metadata.xpath.datastores.api;

import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.Tree;
import gr.cite.femme.exceptions.MetadataIndexException;

import java.io.IOException;
import java.util.List;

public interface MetadataIndexDatastore {

	public void close() throws IOException;

	public void indexMetadatum(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException;

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree) throws MetadataIndexException;

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException;

}
