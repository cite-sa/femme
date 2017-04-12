package gr.cite.femme.engine.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ElementCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		/*if (clazz.equals(Element.class) || clazz.equals(DataElement.class) || clazz.equals(Collection.class) || clazz.equals(DataElementMongo.class)) {
			return (Codec<T>) new ElementCodec(registry);
		} else {
			return null;
		}*/
		return null;
	}
}
