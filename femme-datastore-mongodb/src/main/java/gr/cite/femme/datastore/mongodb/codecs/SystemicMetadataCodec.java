package gr.cite.femme.datastore.mongodb.codecs;

import java.time.Instant;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import gr.cite.femme.core.SystemicMetadata;

public class SystemicMetadataCodec implements CollectibleCodec<SystemicMetadata>{
	private static final String SYSTEMIC_METADATA_ID_KEY = "_id";
	private static final String SYSTEMIC_METADATA_CREATED_KEY = "created";
	private static final String SYSTEMIC_METADATA_MODIFIED_KEY = "modified";
	/*private static final String SYSTEMIC_METADATA_TIMEZONE_OFFSET_KEY = "timezoneOffset";*/
	
	public SystemicMetadataCodec() {
	}
	
	@Override
	public void encode(BsonWriter writer, SystemicMetadata value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (encoderContext.isEncodingCollectibleDocument()) {
			if (!documentHasId(value)) {
				generateIdIfAbsentFromDocument(value);
			}
			
			writer.writeObjectId(SYSTEMIC_METADATA_ID_KEY, new ObjectId(value.getId()));
			writer.writeDateTime(SYSTEMIC_METADATA_CREATED_KEY, Instant.now().toEpochMilli());
			writer.writeDateTime(SYSTEMIC_METADATA_MODIFIED_KEY, Instant.now().toEpochMilli());
		}
		writer.writeEndDocument();
	}
	
	@Override
	public Class<SystemicMetadata> getEncoderClass() {
		return SystemicMetadata.class;
	}
	@Override
	public SystemicMetadata decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartDocument();
		
		String id = reader.readObjectId(SYSTEMIC_METADATA_ID_KEY).toString();
		Instant created = Instant.ofEpochMilli(reader.readDateTime(SYSTEMIC_METADATA_CREATED_KEY));
		Instant modified = Instant.ofEpochMilli(reader.readDateTime(SYSTEMIC_METADATA_MODIFIED_KEY));
		
		reader.readEndDocument();
		
		return new SystemicMetadata(id, created, modified);
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
