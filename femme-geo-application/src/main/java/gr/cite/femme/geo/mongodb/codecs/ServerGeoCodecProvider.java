package gr.cite.femme.geo.mongodb.codecs;

import gr.cite.femme.geo.core.ServerGeo;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ServerGeoCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz.equals(ServerGeo.class)) {
			return (Codec<T>) new ServerGeoCodec(registry);
		} else {
			return null;
		}
	}
}
