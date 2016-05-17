package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MetadatumJsonCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> metadatumJsonClass, CodecRegistry registry) {
		if (metadatumJsonClass == MetadatumJson.class) {
			return (Codec<T>) new MetadatumJsonCodec(registry);
		}
		return null;
	}
}
