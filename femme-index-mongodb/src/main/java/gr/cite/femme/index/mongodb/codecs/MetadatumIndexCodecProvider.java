package gr.cite.femme.index.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.femme.index.api.client.MetadatumIndex;


public class MetadatumIndexCodecProvider implements CodecProvider {

	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == MetadatumIndex.class) {
			return (Codec<T>) new MetadatumIndexCodec(registry);
		}
		return null;
	}

}
