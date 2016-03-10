package gr.cite.exmms.datastore.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.gridfs.GridFSBucket;

import gr.cite.exmms.core.Metadatum;

public class MetadatumCodecProvider implements CodecProvider {
	private GridFSBucket gridFSBucket;
	
	public MetadatumCodecProvider(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == Metadatum.class) {
			return (Codec<T>) new MetadatumCodec(this.gridFSBucket);
		}
		return null;
	}
}
