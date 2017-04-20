package gr.cite.femme.engine.metadatastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.codecs.MetadatumJson;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadatumJsonCodec implements CollectibleCodec<MetadatumJson> {
	private static final Logger logger = LoggerFactory.getLogger(MetadatumJsonCodec.class);
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	private static final String METADATUM_VALUE_KEY = "metadata";

	private CodecRegistry codecRegistry;

	public MetadatumJsonCodec() {

	}

	public MetadatumJsonCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, MetadatumJson value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		if (value.getId() != null) {
			writer.writeObjectId(METADATUM_ID_KEY, new ObjectId(value.getId()));
		}
		
		if (value.getElementId() != null) {
			writer.writeObjectId(METADATUM_ELEMENT_ID_KEY, new ObjectId(value.getElementId()));
		}
		
		if (value.getName() != null) {
			writer.writeString(METADATUM_NAME_KEY, value.getName());
		}
		
		if (value.getContentType() != null) {
			writer.writeString(METADATUM_CONTENT_TYPE_KEY, value.getContentType());
		}
		
		if (value.getValue() != null) {
			Document doc = Document.parse(value.getValue());
			writer.writeName(METADATUM_VALUE_KEY);
			encoderContext.encodeWithChildContext(codecRegistry.get(Document.class), writer, doc);
		}

		writer.writeEndDocument();
	}

	@Override
	public Class<MetadatumJson> getEncoderClass() {
		return MetadatumJson.class;
	}

	@Override
	public MetadatumJson decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, elementId = null, name = null, contentType = null, value = null;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(METADATUM_ID_KEY)) {
            	id = reader.readObjectId().toString();
            } else if (fieldName.equals(METADATUM_ELEMENT_ID_KEY)) {
            	elementId = reader.readObjectId().toString();
            } else if (fieldName.equals(METADATUM_NAME_KEY)) {
            	name = reader.readString();
            } else if (fieldName.equals(METADATUM_CONTENT_TYPE_KEY)) {
            	contentType = reader.readString();
            }  else if (fieldName.equals(METADATUM_VALUE_KEY)) {
            	if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		value = codecRegistry.get(Document.class).decode(reader, decoderContext).toJson();
            	}
            }
        }

        reader.readEndDocument();

		MetadatumJson metadatumJson = new MetadatumJson();
		metadatumJson.setId(id);
		metadatumJson.setElementId(elementId);
		metadatumJson.setName(name);
		metadatumJson.setValue(value);metadatumJson.setContentType(contentType);
        
        return metadatumJson;
	}

	@Override
	public MetadatumJson generateIdIfAbsentFromDocument(MetadatumJson metadatum) {
		if (!documentHasId(metadatum)) {
			metadatum.setId(new ObjectId().toString());
		}
		return metadatum;
	}

	@Override
	public boolean documentHasId(MetadatumJson metadatum) {
		return metadatum.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(MetadatumJson metadatum) {
		if (!documentHasId(metadatum)) {
			throw new IllegalStateException("The metadatum does not contain an _id");
		}
		return new BsonString(metadatum.getId());
	}
}
