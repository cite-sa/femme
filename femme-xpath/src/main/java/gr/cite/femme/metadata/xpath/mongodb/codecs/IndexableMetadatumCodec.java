package gr.cite.femme.metadata.xpath.mongodb.codecs;

import gr.cite.femme.metadata.xpath.core.IndexableMetadatum;
import org.bson.*;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

public class IndexableMetadatumCodec implements CollectibleCodec<IndexableMetadatum>{

    private static final String ID_KEY = "_id";
    private static final String METADATUM_ID_KEY = "metadatumId";
    private static final String ELEMENT_ID_KEY = "elementId";
    private static final String ORIGINAL_CONTENT_TYPE_KEY = "originalContentType";
    private static final String VALUE_KEY = "value";

    private CodecRegistry codecRegistry;


    public IndexableMetadatumCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    @Override
    public void encode(BsonWriter writer, IndexableMetadatum value, EncoderContext encoderContext) {
        generateIdIfAbsentFromDocument(value);

        writer.writeStartDocument();
        if (value.getId() != null) {
            writer.writeObjectId(IndexableMetadatumCodec.ID_KEY, new ObjectId(value.getId()));
        }
        if (value.getMetadatumId() != null) {
            writer.writeObjectId(IndexableMetadatumCodec.METADATUM_ID_KEY, new ObjectId(value.getMetadatumId()));
        }
        if (value.getElementId() != null) {
            writer.writeObjectId(IndexableMetadatumCodec.ELEMENT_ID_KEY, new ObjectId(value.getElementId()));
        }
        if (value.getOriginalContentType() != null) {
            writer.writeString(IndexableMetadatumCodec.ORIGINAL_CONTENT_TYPE_KEY, value.getOriginalContentType());
        }
        if (value.getValue() != null) {
            writer.writeName(IndexableMetadatumCodec.VALUE_KEY);
            encoderContext.encodeWithChildContext(codecRegistry.get(Document.class), writer, Document.parse(value.getValue()));
        }
        writer.writeEndDocument();
    }

    @Override
    public IndexableMetadatum decode(BsonReader reader, DecoderContext decoderContext) {
        String id = null, metadatumId = null, elementId = null, originalContentType = null, value = null;

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals(IndexableMetadatumCodec.ID_KEY)) {
                id = reader.readObjectId().toString();
            } else if (fieldName.equals(IndexableMetadatumCodec.METADATUM_ID_KEY)) {
                metadatumId = reader.readObjectId().toString();
            } else if (fieldName.equals(IndexableMetadatumCodec.ELEMENT_ID_KEY)) {
                elementId = reader.readObjectId().toString();
            } else if (fieldName.equals(IndexableMetadatumCodec.ORIGINAL_CONTENT_TYPE_KEY)) {
                originalContentType = reader.readString();
            } else if (fieldName.equals(IndexableMetadatumCodec.VALUE_KEY)) {
                Document valueDocument = codecRegistry.get(Document.class).decode(reader, decoderContext);
                value = valueDocument.toJson();
            }
        }
        reader.readEndDocument();

        IndexableMetadatum metadatum = new IndexableMetadatum();
        metadatum.setId(id);
        metadatum.setMetadatumId(metadatumId);
        metadatum.setElementId(elementId);
        metadatum.setOriginalContentType(originalContentType);
        metadatum.setValue(value);
        return metadatum;
    }

    @Override
    public Class<IndexableMetadatum> getEncoderClass() {
        return IndexableMetadatum.class;
    }

    @Override
    public boolean documentHasId(IndexableMetadatum indexableMetadatum) {
        return indexableMetadatum.getId() != null;
    }

    @Override
    public IndexableMetadatum generateIdIfAbsentFromDocument(IndexableMetadatum indexableMetadatum) {
        if (!documentHasId(indexableMetadatum)) {
            indexableMetadatum.setId(new ObjectId().toString());
        }
        return indexableMetadatum;
    }

    @Override
    public BsonValue getDocumentId(IndexableMetadatum indexableMetadatum) {
        if (!documentHasId(indexableMetadatum)) {
            throw new IllegalStateException("The transformed metadatum does not contain an _id");
        }
        return new BsonString(indexableMetadatum.getId());
    }
}
