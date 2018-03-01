package gr.cite.femme.geo.mongodb.codecs;

import gr.cite.femme.core.geo.CoverageGeo;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CoverageGeoCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz.equals(CoverageGeo.class)) {
			return (Codec<T>) new CoverageGeoCodec(registry);
		} else {
			return null;
		}
	}
}
