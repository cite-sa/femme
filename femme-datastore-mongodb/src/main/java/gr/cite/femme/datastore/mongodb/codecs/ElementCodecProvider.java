package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.femme.datastore.mongodb.core.DataElementMongo;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;

public class ElementCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Element.class || clazz == DataElement.class || clazz == Collection.class || clazz == DataElementMongo.class) {
			return (Codec<T>) new ElementCodec(registry);
		}
		return null;
	}
}
