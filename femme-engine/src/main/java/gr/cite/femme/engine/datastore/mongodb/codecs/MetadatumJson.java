package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.Metadatum;

public class MetadatumJson extends Metadatum {
	public MetadatumJson() {

	}

	public MetadatumJson(Metadatum metadatum) {
		super.setId(metadatum.getId());
		super.setElementId(metadatum.getElementId());
		super.setName(metadatum.getName());
		super.setValue(metadatum.getName());
		super.setContentType(metadatum.getContentType());
	}
}
