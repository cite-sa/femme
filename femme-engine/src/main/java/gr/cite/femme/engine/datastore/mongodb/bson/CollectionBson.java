package gr.cite.femme.engine.datastore.mongodb.bson;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.core.model.Collection;

public class CollectionBson extends ElementBson implements Bson {
	private Collection collection;
	
	public CollectionBson() {
	}
	public CollectionBson(Collection collection) {
		this.collection = collection;
	}
	@Override
    public <T> BsonDocument toBsonDocument(final Class<T> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<>(collection, codecRegistry.get(Collection.class));
    }
}
