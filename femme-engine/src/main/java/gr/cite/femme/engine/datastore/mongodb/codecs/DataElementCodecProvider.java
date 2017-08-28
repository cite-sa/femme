package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.core.DataElementMongo;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Collection;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class DataElementCodecProvider implements CodecProvider {
	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		//if (clazz.equals(Element.class) || clazz.equals(DataElement.class) || clazz.equals(Collection.class) || clazz.equals(DataElementMongo.class)) {
		if (clazz.equals(DataElement.class)) {
			return (Codec<T>) new DataElementCodec(registry);
		} else {
			return null;
		}
	}
}
