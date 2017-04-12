package gr.cite.femme.index.mongodb.codecs;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import gr.cite.femme.index.api.client.MetadatumIndex;

public class MetadatumIndexCodec implements CollectibleCodec<MetadatumIndex> {
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String ELEMENT_ID_KEY = "elementId";
	private static final String CONTENT_TYPE_KEY = "contentType";
	private static final String VALUE_KEY = "value";
	
	private CodecRegistry codecRegistry;
	
	public MetadatumIndexCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public void encode(BsonWriter writer, MetadatumIndex value, EncoderContext encoderContext) {
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}			
		
		writer.writeStartDocument();
		if (value.getId() != null) {			
			writer.writeObjectId(METADATUM_ID_KEY, new ObjectId(value.getId()));
		}
		if (value.getElementId() != null) {
			writer.writeObjectId(ELEMENT_ID_KEY, new ObjectId(value.getElementId()));
		}
		if (value.getContentType() != null) {
			writer.writeString(CONTENT_TYPE_KEY, value.getContentType());
		}
		if (value.getValue() != null) {
			writer.writeName(VALUE_KEY);
			encoderContext.encodeWithChildContext(codecRegistry.get(Document.class), writer, Document.parse(value.getValue()));
		}
		writer.writeEndDocument();
	}

	@Override
	public Class<MetadatumIndex> getEncoderClass() {
		return MetadatumIndex.class;
	}

	@Override
	public MetadatumIndex decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, elementId = null, contentType = null, value = null;
		Document valueDocument;
		
		reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(METADATUM_ID_KEY)) {
            	id = reader.readObjectId().toString();
            } else if (fieldName.equals(ELEMENT_ID_KEY)) {
            	elementId = reader.readString();
            } else if (fieldName.equals(CONTENT_TYPE_KEY)) {
            	contentType = reader.readString();
            } else if (fieldName.equals(VALUE_KEY)) {
        		valueDocument = codecRegistry.get(Document.class).decode(reader, decoderContext);
        		value = valueDocument.toJson();
            }
        }
        reader.readEndDocument();
        
        return new MetadatumIndex(id, elementId, contentType, value);
	}

	@Override
	public boolean documentHasId(MetadatumIndex value) {
		return value.getId() != null;
	}

	@Override
	public MetadatumIndex generateIdIfAbsentFromDocument(MetadatumIndex value) {
		if (!documentHasId(value)) {
			value.setId(new ObjectId().toString());
		}
		return value;
	}

	@Override
	public BsonValue getDocumentId(MetadatumIndex value) {
		if (!documentHasId(value)) {
			throw new IllegalStateException("The metadatum index does not contain an _id");				
	    }
	    return new BsonString(value.getId());
	}

}
