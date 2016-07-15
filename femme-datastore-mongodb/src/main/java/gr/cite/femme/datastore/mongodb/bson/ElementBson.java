package gr.cite.femme.datastore.mongodb.bson;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import gr.cite.femme.model.Element;

public class ElementBson implements Bson {
	private Element element;
	
	public ElementBson() {
	}
	
	public ElementBson(Element element) {
		this.element = element;
	}
	
	@Override
    public <c> BsonDocument toBsonDocument(final Class<c> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<Element>(element, codecRegistry.get(Element.class));
    }
}
