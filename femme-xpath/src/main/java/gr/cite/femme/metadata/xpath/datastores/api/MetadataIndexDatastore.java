package gr.cite.femme.metadata.xpath.datastores.api;

import gr.cite.femme.metadata.xpath.ReIndexingProcess;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.metadata.xpath.elasticsearch.utils.Tree;
import gr.cite.femme.exceptions.MetadataIndexException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface MetadataIndexDatastore {

	public void close() throws IOException;

	public void index(IndexableMetadatum indexableMetadatum, MetadataSchema metadataSchema) throws MetadataIndexException;

	public ReIndexingProcess retrieveReIndexer(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore);

	/*public void beginReIndexing() throws MetadataIndexException;

	public void endReIndexing() throws MetadataIndexException;*/

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree) throws MetadataIndexException;

	public List<IndexableMetadatum> query(Tree<QueryNode> queryTree, boolean lazy) throws MetadataIndexException;

}
