package gr.cite.femme.engine.metadata.xpath.mongodb.codecs;

import gr.cite.femme.engine.metadata.xpath.core.MaterializedPathsNode;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MaterializedPathsNodeCodecProvider implements CodecProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == MaterializedPathsNode.class) {
            return (Codec<T>) new MaterializedPathsNodeCodec();
        }
        return null;
    }
}
