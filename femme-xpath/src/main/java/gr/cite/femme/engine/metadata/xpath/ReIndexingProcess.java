package gr.cite.femme.engine.metadata.xpath;

import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.model.Metadatum;

public interface ReIndexingProcess {

	public void begin() throws MetadataIndexException;

	public void end() throws MetadataIndexException;

	public boolean reIndexingInProgress();

	public void index(Metadatum metadatum) throws UnsupportedOperationException, MetadataIndexException;
}
