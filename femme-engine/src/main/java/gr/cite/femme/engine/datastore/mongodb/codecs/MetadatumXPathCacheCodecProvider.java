package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.MetadatumXPathCache;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MetadatumXPathCacheCodecProvider implements CodecProvider {

	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == MetadatumXPathCache.class) {
			return (Codec<T>) new MetadatumXPathCacheCodec();
		}
		return null;
	}

}
