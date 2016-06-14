package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.core.MetadatumXPathCache;

public class MetadatumXPathCacheCodecProvider implements CodecProvider {

	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == MetadatumXPathCache.class) {
			return (Codec<T>) new MetadatumXPathCacheCodec();
		}
		return null;
	}

}
