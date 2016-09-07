package gr.cite.femme.datastore.mongodb.core;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.model.DataElement;

public class DataElementMongo extends DataElement implements Bson {

	public DataElementMongo() {

	}

	public DataElementMongo(DataElement dataElement) {
		super(dataElement.getId(), dataElement.getName(), dataElement.getEndpoint(), dataElement.getMetadata(),
				dataElement.getSystemicMetadata(), dataElement.getDataElements(), dataElement.getCollections());

		/*
		 * dataElement.builder() .id(dataElement.getId())
		 * .endpoint(dataElement.getEndpoint()) .name(dataElement.getName())
		 * .dataElements(dataElement.getDataElements())
		 * .collections(dataElement.getCollections()).build();
		 */
	}

	@Override
	public <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry) {
		return new BsonDocumentWrapper<DataElementMongo>(this, codecRegistry.get(DataElementMongo.class));
	}

}
