package gr.cite.femme.datastore.mongodb.core;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.model.DataElement;

public class DataElementMongo extends DataElement implements Bson {

	public DataElementMongo() {

	}

	/*public DataElementMongo(DataElement dataElement) {
		super(dataElement.getId(), dataElement.getName(), dataElement.getEndpoint(), dataElement.getMetadata(),
				dataElement.getSystemicMetadata(), dataElement.getDataElements(), dataElement.getCollections());
	}*/

	@Override
	public <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry) {
		return new BsonDocumentWrapper<DataElementMongo>(this, codecRegistry.get(DataElementMongo.class));
	}

}
