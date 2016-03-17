package gr.cite.femme.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.gridfs.GridFSBucket;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.mongodb.gridfs.MetadatumGridFS;

public class MetadatumCodecProvider implements CodecProvider {
	private MetadatumGridFS metadatumGridFS;
	
	public MetadatumCodecProvider(MetadatumGridFS metadatumGridFS) {
		this.metadatumGridFS = metadatumGridFS;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Metadatum.class) {
			return (Codec<T>) new MetadatumCodec(this.metadatumGridFS);
		}
		return null;
	}
}
