package gr.cite.femme.engine.datastore.mongodb.bson;

import gr.cite.femme.core.model.Element;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public class ElementBson implements Bson {
	private Element element;
	
	public ElementBson() {
	}
	
	public ElementBson(Element element) {
		this.element = element;
	}
	
	@Override
    public <c> BsonDocument toBsonDocument(final Class<c> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<>(element, codecRegistry.get(Element.class));
    }
}
