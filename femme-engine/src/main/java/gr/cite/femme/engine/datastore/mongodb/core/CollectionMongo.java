package gr.cite.femme.engine.datastore.mongodb.core;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.core.model.Collection;

public class CollectionMongo extends Collection implements Bson {

	public CollectionMongo() {

	}

	/*public CollectionMongo(Collection collection) {
		
		super(collection.getId(), collection.getName(), collection.getEndpoint(), collection.getMetadata(),
				collection.getSystemicMetadata(), collection.getDataElements());
	}*/

	@Override
	public <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry) {
		return new BsonDocumentWrapper<CollectionMongo>(this, codecRegistry.get(CollectionMongo.class));
	}
}
