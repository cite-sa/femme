package gr.cite.femme.engine.metadata.xpath.datastores.api;

import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.engine.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Tree;

import java.io.IOException;
import java.util.List;

public interface MetadataIndexDatastore {

	public void close() throws IOException;

	public void insert(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException;

	public void delete(String metadatumId) throws MetadataIndexException;

	public void deleteByElementId(String elementId) throws MetadataIndexException;

	public void delete(String field, String value) throws MetadataIndexException;

	public ReIndexingProcess retrieveReIndexer(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore);

	/*public void beginReIndexing() throws MetadataIndexException;

	public void endReIndexing() throws MetadataIndexException;*/

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree) throws MetadataIndexException;

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException;

	public List<IndexableMetadatum> query(List<String> elementIds, Tree<QueryNode> queryTree) throws MetadataIndexException;

	public List<IndexableMetadatum> query(List<String> elementIds, Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException;

}
