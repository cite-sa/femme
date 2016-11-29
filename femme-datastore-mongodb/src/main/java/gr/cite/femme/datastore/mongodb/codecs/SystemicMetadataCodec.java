
package gr.cite.femme.datastore.mongodb.codecs;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

import com.mongodb.util.JSON;

import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.model.BBox;
import gr.cite.femme.model.Element;
/*import gr.cite.femme.model.DateTime;*/
import gr.cite.femme.model.SystemicMetadata;

public class SystemicMetadataCodec implements CollectibleCodec<SystemicMetadata>{
	/*private static final String SYSTEMIC_METADATA_ID_KEY = "_id";
	private static final String SYSTEMIC_METADATA_CREATED_KEY = "created";
	private static final String SYSTEMIC_METADATA_MODIFIED_KEY = "modified";
	private static final String SYSTEMIC_METADATA_TIMESTAMP_KEY = "timestamp";
	private static final String SYSTEMIC_METADATA_OFFSET_ID_KEY = "offsetId";
	private static final String SYSTEMIC_METADATA_ZONE_ID_KEY = "zoneId";*/
	
	private CodecRegistry codecRegistry;
	
	public SystemicMetadataCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public void encode(BsonWriter writer, SystemicMetadata value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		/*if (encoderContext.isEncodingCollectibleDocument()) {*/
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		if (value.getId() != null) {
			writer.writeObjectId(FieldNames.ID, new ObjectId(value.getId()));			
		}
		if (value.getCreated() != null) {
			writer.writeDateTime(FieldNames.CREATED, value.getCreated().toEpochMilli());
		}
		if (value.getModified() != null) {
			writer.writeDateTime(FieldNames.MODIFIED, value.getModified().toEpochMilli());
		}
		if (value.getOther() != null && value.getOther().size() > 0) {
			writer.writeName("other");
			writer.writeStartDocument();

//	        writer.writeObjectId(FieldNames.ID, new ObjectId());

	        for (final Map.Entry<String, Object> entry : value.getOther().entrySet()) {
        		writer.writeName(entry.getKey());
        		encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse((String)entry.getValue()));
	        }
	        writer.writeEndDocument();
		}
		
		
		
		/*}*/
		writer.writeEndDocument();
	}
	
	/*private void encodeDateTime(BsonWriter writer, DateTime zonedDateTime) {
		writer.writeStartDocument();
		writer.writeDateTime(SYSTEMIC_METADATA_TIMESTAMP_KEY, zonedDateTime.getZonedDateTime().toInstant().toEpochMilli());
		writer.writeString(SYSTEMIC_METADATA_OFFSET_ID_KEY, zonedDateTime.getZonedDateTime().getOffset().getId());
		writer.writeString(SYSTEMIC_METADATA_ZONE_ID_KEY, zonedDateTime.getZonedDateTime().getZone().getId());
		writer.writeEndDocument();
	}*/
	
	@Override
	public Class<SystemicMetadata> getEncoderClass() {
		return SystemicMetadata.class;
	}
	@Override
	public SystemicMetadata decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null;
		Instant created = null, modified = null;
		Map<String, Object> other = null;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
		
			if (fieldName.equals(FieldNames.ID)) {
	        	id = reader.readObjectId().toString();
	        } else if (fieldName.equals(FieldNames.CREATED)) {
	        	created = Instant.ofEpochMilli(reader.readDateTime());
	        } else if (fieldName.equals(FieldNames.MODIFIED)) {
	        	modified = Instant.ofEpochMilli(reader.readDateTime());
	        } else if (fieldName.equals("other")) {
	        	other = new HashMap<>();
	        	reader.readStartDocument();
	        	while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
	        		String otherFieldName = reader.readName();
	        		if ("bbox".equals(otherFieldName)) {
		        		Document value = null;
		        		if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
		            		value = codecRegistry.get(Document.class).decode(reader, decoderContext);
		            	}
		        		BBox bbox = new BBox(value.getString("crs"), ((Document)value.get("geoJson")).toJson());
		                other.put(otherFieldName, bbox);
	        		}
	        	}
	        	reader.readEndDocument();
	        }
        }
		
		reader.readEndDocument();
		
		return new SystemicMetadata(id, created, modified, other);
	}
	
	/*private DateTime decodeZonedDateTime(BsonReader reader) {
		long timestamp = 0;
		String offsetId = null, zoneId = null;
		
		reader.readStartDocument();
		timestamp = reader.readDateTime(SYSTEMIC_METADATA_TIMESTAMP_KEY);
		offsetId = reader.readString(SYSTEMIC_METADATA_OFFSET_ID_KEY);
		zoneId = reader.readString(SYSTEMIC_METADATA_ZONE_ID_KEY);
		reader.readEndDocument();
		
		return new DateTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zoneId)));
	}*/
	
	@Override
	public SystemicMetadata generateIdIfAbsentFromDocument(SystemicMetadata systemicMetadata) {
		if (!documentHasId(systemicMetadata)) {
			systemicMetadata.setId(new ObjectId().toString());
		}
		return systemicMetadata;
	}
	@Override
	public boolean documentHasId(SystemicMetadata systemicMetadata) {
		return systemicMetadata.getId() != null;
	}
	@Override
	public BsonValue getDocumentId(SystemicMetadata systemicMetadata)
	{
	    if (!documentHasId(systemicMetadata))
	    {
	        throw new IllegalStateException("The systemic metadata do not contain an _id");
	    }
	    return new BsonString(systemicMetadata.getId());
	}
}
