package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.Collection;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CollectionCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz.equals(Collection.class)) {
			return (Codec<T>) new CollectionCodec(registry);
		}
		return null;
	}
}
