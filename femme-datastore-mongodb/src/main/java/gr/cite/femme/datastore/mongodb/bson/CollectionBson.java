package gr.cite.femme.datastore.mongodb.bson;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.core.Collection;

public class CollectionBson extends ElementBson implements Bson {
	private Collection collection;
	
	public CollectionBson() {
	}
	public CollectionBson(Collection collection) {
		this.collection = collection;
	}
	@Override
    public <c> BsonDocument toBsonDocument(final Class<c> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<Collection>(collection, codecRegistry.get(Collection.class));
    }
}
