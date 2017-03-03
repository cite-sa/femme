package gr.cite.femme.metadata.xpath.mongodb.codecs;

import gr.cite.femme.metadata.xpath.core.MaterializedPathsNode;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MetadataSchemaCodecProvider implements CodecProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == MetadataSchema.class) {
            return (Codec<T>) new MetadataSchemaCodec();
        }
        return null;
    }
}
