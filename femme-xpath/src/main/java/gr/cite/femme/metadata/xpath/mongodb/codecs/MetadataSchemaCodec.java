package gr.cite.femme.metadata.xpath.mongodb.codecs;

import gr.cite.commons.metadata.analyzer.core.JSONPath;
import gr.cite.femme.metadata.xpath.core.MetadataSchema;
import org.bson.*;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

public class MetadataSchemaCodec implements CollectibleCodec<MetadataSchema> {

    private static final String ID_KEY = "_id";
    private static final String METADATA_SCHEMA_SCHEMA_KEY = "schema";
    private static final String METADATA_SCHEMA_HASH_KEY = "checksum";
    private static final String METADATA_SCHEMA_PATH_KEY = "path";
    private static final String METADATA_SCHEMA_PATH_ARRAY_KEY = "array";

    @Override
    public void encode(BsonWriter writer, MetadataSchema value, EncoderContext encoderContext) {
        generateIdIfAbsentFromDocument(value);

        writer.writeStartDocument();
        if (value.getId() != null) {
            writer.writeObjectId(MetadataSchemaCodec.ID_KEY, new ObjectId(value.getId()));
        }
        if (value.getSchema() != null && value.getSchema().size() > 0) {
            writer.writeStartArray(MetadataSchemaCodec.METADATA_SCHEMA_SCHEMA_KEY);
            for (JSONPath jsonPath: value.getSchema()) {
                writer.writeStartDocument();
                writer.writeString(MetadataSchemaCodec.METADATA_SCHEMA_PATH_KEY, jsonPath.getPath());
                writer.writeBoolean(MetadataSchemaCodec.METADATA_SCHEMA_PATH_ARRAY_KEY, jsonPath.isArray());
                writer.writeEndDocument();
            }
            writer.writeEndArray();
        }
        if (value.getChecksum() != null) {
            writer.writeString(MetadataSchemaCodec.METADATA_SCHEMA_HASH_KEY, value.getChecksum());
        }
        writer.writeEndDocument();
    }

    @Override
    public MetadataSchema decode(BsonReader reader, DecoderContext decoderContext) {
        String id = null, hash = null;
        Set<JSONPath> schema = null;

        reader.readStartDocument();
        BsonType currentBsonType;
        while ((currentBsonType = reader.readBsonType()) != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (!BsonType.NULL.equals(currentBsonType)) {
                if (fieldName.equals(MetadataSchemaCodec.ID_KEY)) {
                    id = reader.readObjectId().toString();
                } else if (fieldName.equals(MetadataSchemaCodec.METADATA_SCHEMA_SCHEMA_KEY)) {
                    schema = new HashSet<>();
                    reader.readStartArray();
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        reader.readStartDocument();
                        schema.add(new JSONPath(reader.readString(MetadataSchemaCodec.METADATA_SCHEMA_PATH_KEY), reader.readBoolean()));
                        reader.readEndDocument();
                    }
                    reader.readEndArray();
                } else if (fieldName.equals(MetadataSchemaCodec.METADATA_SCHEMA_HASH_KEY)) {
                    hash = reader.readString();
                }
            } else {
                reader.readNull();
            }
        }
        reader.readEndDocument();

        MetadataSchema metadataSchema = new MetadataSchema();
        metadataSchema.setId(id);
        metadataSchema.setSchema(schema);
        metadataSchema.setChecksum(hash);
        return metadataSchema;
    }

    @Override
    public Class<MetadataSchema> getEncoderClass() {
        return MetadataSchema.class;
    }

    @Override
    public MetadataSchema generateIdIfAbsentFromDocument(MetadataSchema schema) {
        if (!documentHasId(schema)) {
            schema.setId(new ObjectId().toString());
        }
        return schema;
    }

    @Override
    public boolean documentHasId(MetadataSchema schema) {
        return schema.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(MetadataSchema schema) {
        if (!documentHasId(schema)) {
            throw new IllegalStateException("The metadata schema does not contain an _id");
        }
        return new BsonString(schema.getId());
    }

}
