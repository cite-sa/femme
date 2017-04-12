package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.engine.metadatastore.mongodb.MetadataGridFSFile;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MetadataGridFSFileCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == MetadataGridFSFile.class) {
			return (Codec<T>) new MetadataGridFSFileCodec(registry);
		}
		return null;
	}
}
