package gr.cite.femme.datastore.mongodb.codecs;

import gr.cite.femme.model.SystemicMetadata;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.model.Metadatum;

public class MetadatumCodec implements CollectibleCodec<Metadatum> {
	private CodecRegistry codecRegistry;

	public MetadatumCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Metadatum value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		
		if (value.getId() != null) {
			writer.writeObjectId(FieldNames.ID, new ObjectId(value.getId()));
			/*writer.writeObjectId(METADATUM_FILE_ID_KEY, new ObjectId(value.getId()));*/			
		}
		/*if (value.getElementId() != null) {
			writer.writeObjectId(FieldNames.METADATA_ELEMENT_ID, new ObjectId(value.getElementId()));
		}*/
		/* writer.writeString(METADATUM_FILENAME_KEY, file.getSecond()); */
		/*if (value.getName() != null) {
			writer.writeString(FieldNames.NAME, value.getName());
		}*/
		/*if (value.getContentType() != null) {
			writer.writeString(FieldNames.CONTENT_TYPE, value.getContentType());
		}*/
		/*if (value.getSystemicMetadata() != null) {
			writer.writeName(FieldNames.SYSTEMIC_METADATA);
			encoderContext.encodeWithChildContext(codecRegistry.get(SystemicMetadata.class), writer, value.getSystemicMetadata());
		}*/

		/*if (value.getXPathCache() != null) {
			writer.writeStartArray(METADATUM_XPATH_CACHE_KEY);
			
			for (MetadatumXPathCache metadatumIndex: value.getXPathCache()) {
				encoderContext.encodeWithChildContext(codecRegistry.get(MetadatumXPathCache.class), writer, metadatumIndex);
			}
				
			writer.writeEndArray();
		}*/
		
		writer.writeEndDocument();
	}

	@Override
	public Class<Metadatum> getEncoderClass() {
		return Metadatum.class;
	}

	@Override
	public Metadatum decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, name = null, contentType = null, elementId = null;
		SystemicMetadata systemicMetadata = null;
		/*List<MetadatumXPathCache> metadatumIndexes = null;*/
		
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(FieldNames.ID)) {
            	id = reader.readObjectId().toString();
            }/* else if (fieldName.equals(FieldNames.METADATA_ELEMENT_ID)) {
            	elementId = reader.readObjectId().toString();
            } else if (fieldName.equals(FieldNames.NAME)) {
            	name = reader.readString();
            } else if (fieldName.equals(FieldNames.CONTENT_TYPE)) {
            	contentType = reader.readString();
            } else if (fieldName.equals(FieldNames.SYSTEMIC_METADATA)) {
				if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
					systemicMetadata = codecRegistry.get(SystemicMetadata.class).decode(reader, decoderContext);
				}
			}*//* else if (fieldName.equals(METADATUM_XPATH_CACHE_KEY)) {
            	metadatumIndexes = new ArrayList<>();
            	
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			metadatumIndexes.add((MetadatumXPathCache) codecRegistry.get(MetadatumXPathCache.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            }*/
		}
		
		reader.readEndDocument();

		Metadatum metadatum = new Metadatum();
		metadatum.setId(id);
		/*metadatum.setElementId(elementId);
		metadatum.setName(name);
		metadatum.setContentType(contentType);
		metadatum.setSystemicMetadata(systemicMetadata)*/;
		/*metadatum.setXPathCache(metadatumIndexes);*/

		return metadatum;
	}

	@Override
	public Metadatum generateIdIfAbsentFromDocument(Metadatum metadatum) {
		if (!documentHasId(metadatum)) {
			metadatum.setId(new ObjectId().toString());
		}
		return metadatum;
	}

	@Override
	public boolean documentHasId(Metadatum metadatum) {
		return metadatum.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(Metadatum metadatum) {
		if (!documentHasId(metadatum)) {
			throw new IllegalStateException("The metadatum does not contain an _id");
		}
		return new BsonString(metadatum.getId());
	}
}
