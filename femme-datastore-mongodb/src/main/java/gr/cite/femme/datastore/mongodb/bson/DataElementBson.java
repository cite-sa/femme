package gr.cite.femme.datastore.mongodb.bson;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.core.DataElement;

public class DataElementBson extends ElementBson implements Bson {
	DataElement dataElement;
	
	public DataElementBson() {
	}
	public DataElementBson(DataElement dataElement) {
		this.dataElement = dataElement;
	}
	@Override
    public <c> BsonDocument toBsonDocument(final Class<c> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<DataElement>(dataElement, codecRegistry.get(DataElement.class));
    }
}