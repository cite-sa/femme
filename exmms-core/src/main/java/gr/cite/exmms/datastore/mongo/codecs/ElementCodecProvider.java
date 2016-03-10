package gr.cite.exmms.datastore.mongo.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;

public class ElementCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Element.class || clazz == DataElement.class || clazz == Collection.class) {
			return (Codec<T>) new ElementCodec(registry);
		}
		return null;
	}
}
