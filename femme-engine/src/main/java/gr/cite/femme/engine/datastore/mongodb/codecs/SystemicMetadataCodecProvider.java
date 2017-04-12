package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.SystemicMetadata;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class SystemicMetadataCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == SystemicMetadata.class) {
			return (Codec<T>) new SystemicMetadataCodec(registry);
		}
		return null;
	}
}
