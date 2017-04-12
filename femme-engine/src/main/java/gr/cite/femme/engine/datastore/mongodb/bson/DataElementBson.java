package gr.cite.femme.engine.datastore.mongodb.bson;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.core.model.DataElement;

public class DataElementBson extends ElementBson implements Bson {
	DataElement dataElement;
	
	public DataElementBson() {
	}

	public DataElementBson(DataElement dataElement) {
		this.dataElement = dataElement;
	}

	@Override
    public <T> BsonDocument toBsonDocument(final Class<T> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<>(dataElement, codecRegistry.get(DataElement.class));
    }
}