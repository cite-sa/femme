package gr.cite.femme.datastore.mongodb.codecs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import gr.cite.femme.model.DateTime;
import gr.cite.femme.model.SystemicMetadata;

public class SystemicMetadataCodec implements CollectibleCodec<SystemicMetadata>{
	private static final String SYSTEMIC_METADATA_ID_KEY = "_id";
	private static final String SYSTEMIC_METADATA_CREATED_KEY = "created";
	private static final String SYSTEMIC_METADATA_MODIFIED_KEY = "modified";
	private static final String SYSTEMIC_METADATA_TIMESTAMP_KEY = "timestamp";
	private static final String SYSTEMIC_METADATA_OFFSET_ID_KEY = "offsetId";
	private static final String SYSTEMIC_METADATA_ZONE_ID_KEY = "zoneId";
	
	public SystemicMetadataCodec() {
	}
	
	@Override
	public void encode(BsonWriter writer, SystemicMetadata value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		/*if (encoderContext.isEncodingCollectibleDocument()) {*/
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		
		if (value.getId() != null) {
			writer.writeObjectId(SYSTEMIC_METADATA_ID_KEY, new ObjectId(value.getId()));			
		}
		
		if (value.getCreated() != null) {
			writer.writeName(SYSTEMIC_METADATA_CREATED_KEY);
			encodeDateTime(writer, value.getCreated());
		}
		
		if (value.getModified() != null) {
			writer.writeName(SYSTEMIC_METADATA_MODIFIED_KEY);
			encodeDateTime(writer, value.getModified());			
		}
		
		/*}*/
		writer.writeEndDocument();
	}
	
	private void encodeDateTime(BsonWriter writer, DateTime zonedDateTime) {
		writer.writeStartDocument();
		writer.writeDateTime(SYSTEMIC_METADATA_TIMESTAMP_KEY, zonedDateTime.getZonedDateTime().toInstant().toEpochMilli());
		writer.writeString(SYSTEMIC_METADATA_OFFSET_ID_KEY, zonedDateTime.getZonedDateTime().getOffset().getId());
		writer.writeString(SYSTEMIC_METADATA_ZONE_ID_KEY, zonedDateTime.getZonedDateTime().getZone().getId());
		writer.writeEndDocument();
	}
	
	
	
	@Override
	public Class<SystemicMetadata> getEncoderClass() {
		return SystemicMetadata.class;
	}
	@Override
	public SystemicMetadata decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null;
		DateTime created = null, modified = null;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
		
			if (fieldName.equals(SYSTEMIC_METADATA_ID_KEY)) {
	        	id = reader.readObjectId().toString();
	        } else if (fieldName.equals(SYSTEMIC_METADATA_CREATED_KEY)) {
	        	created = decodeZonedDateTime(reader);
	        } else if (fieldName.equals(SYSTEMIC_METADATA_MODIFIED_KEY)) {
	        	modified = decodeZonedDateTime(reader);
	        }
        }
		
		reader.readEndDocument();
		
		return new SystemicMetadata(id, created, modified);
	}
	
	private DateTime decodeZonedDateTime(BsonReader reader) {
		long timestamp = 0;
		String offsetId = null, zoneId = null;
		
		reader.readStartDocument();
		timestamp = reader.readDateTime(SYSTEMIC_METADATA_TIMESTAMP_KEY);
		offsetId = reader.readString(SYSTEMIC_METADATA_OFFSET_ID_KEY);
		zoneId = reader.readString(SYSTEMIC_METADATA_ZONE_ID_KEY);
		reader.readEndDocument();
		
		return new DateTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zoneId)));
	}
	
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
