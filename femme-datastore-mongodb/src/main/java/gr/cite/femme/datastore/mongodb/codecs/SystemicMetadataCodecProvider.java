package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.femme.model.SystemicMetadata;

public class SystemicMetadataCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == SystemicMetadata.class) {
			return (Codec<T>) new SystemicMetadataCodec();
		}
		return null;
	}
}
