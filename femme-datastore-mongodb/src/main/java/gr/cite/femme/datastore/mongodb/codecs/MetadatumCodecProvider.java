package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.gridfs.GridFSBucket;

import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.model.Metadatum;

public class MetadatumCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Metadatum.class) {
			return (Codec<T>) new MetadatumCodec(registry);
		}
		return null;
	}
}
