package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.Metadatum;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MetadataGridFSFileMetadataCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Metadatum.class) {
			return (Codec<T>) new MetadataGridFSFileMetadataCodec(registry);
		}
		return null;
	}
}
