package gr.cite.femme.metadata.xpath;

import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.model.Metadatum;

public interface ReIndexingProcess {

	public void begin() throws MetadataIndexException;

	public void end() throws MetadataIndexException;

	public boolean reIndexingInProgress();

	public void index(Metadatum metadatum) throws UnsupportedOperationException, MetadataIndexException;
}
