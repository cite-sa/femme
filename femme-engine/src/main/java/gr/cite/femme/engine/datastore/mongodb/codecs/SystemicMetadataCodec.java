
package gr.cite.femme.engine.datastore.mongodb.codecs;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.model.Status;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import gr.cite.femme.core.model.BBox;
/*import DateTime;*/

public class SystemicMetadataCodec implements Codec<SystemicMetadata> {
	static private final ObjectMapper mapper = new ObjectMapper();
	private CodecRegistry codecRegistry;
	
	public SystemicMetadataCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public void encode(BsonWriter writer, SystemicMetadata value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		/*if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		if (value.getId() != null) {
			writer.writeObjectId(FieldNames.ID, new ObjectId(value.getId()));			
		}*/
		if (value.getCreated() != null) {
			writer.writeDateTime(FieldNames.CREATED, value.getCreated().toEpochMilli());
		}
		if (value.getModified() != null) {
			writer.writeDateTime(FieldNames.MODIFIED, value.getModified().toEpochMilli());
		}
		if (value.getStatus() != null) {
			writer.writeInt32(FieldNames.STATUS, value.getStatus().getStatusCode());
		}
		if (value.getGeo() != null && value.getGeo().size() > 0) {
			writer.writeName("geo");
			writer.writeStartDocument();

			for (final Map.Entry<String, String> entry : value.getGeo().entrySet()) {
				writer.writeName(entry.getKey());
				/*try {*/
				//encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse(mapper.writeValueAsString(entry.getValue())));
				encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse(entry.getValue()));

				/*} catch (JsonProcessingException e) {
					e.printStackTrace();
				}*/
			}
			writer.writeEndDocument();
		}
		if (value.getOther() != null && value.getOther().size() > 0) {
			writer.writeName("other");
			writer.writeStartDocument();

	        for (final Map.Entry<String, Object> entry : value.getOther().entrySet()) {
        		writer.writeName(entry.getKey());
				/*try {*/
					//encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse(mapper.writeValueAsString(entry.getValue())));
					encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse((String) entry.getValue()));

				/*} catch (JsonProcessingException e) {
					e.printStackTrace();
				}*/
			}
	        writer.writeEndDocument();
		}

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
		//String id = null;
		Instant created = null, modified = null;
		Status status = null;
		Map<String, String> geo = null;
		Map<String, Object> other = null;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
		
			/*if (fieldName.equals(FieldNames.ID)) {
	        	id = reader.readObjectId().toString();
	        } else */if (fieldName.equals(FieldNames.CREATED)) {
	        	created = Instant.ofEpochMilli(reader.readDateTime());
	        } else if (fieldName.equals(FieldNames.MODIFIED)) {
	        	modified = Instant.ofEpochMilli(reader.readDateTime());
	        } else if (fieldName.equals(FieldNames.STATUS)) {
				status = Status.getEnum(reader.readInt32());
			} else if (fieldName.equals("geo")) {
				geo = new HashMap<>();
				reader.readStartDocument();
				while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
					String geoFieldName = reader.readName();
					if ("bbox".equals(geoFieldName)) {
						Document value = null;
						if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
							value = this.codecRegistry.get(Document.class).decode(reader, decoderContext);
						}
						//BBox bbox = new BBox(value.getString("crs"), ((Document)value.get("geoJson")).toJson());
						geo.put(geoFieldName, value.toJson());
					}
				}
				reader.readEndDocument();
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

        SystemicMetadata systemicMetadata = new SystemicMetadata();
        systemicMetadata.setCreated(created);
		systemicMetadata.setModified(modified);
		systemicMetadata.setStatus(status);
		systemicMetadata.setGeo(geo);
		systemicMetadata.setOther(other);

		//return new SystemicMetadata(id, created, modified, status, other);
		return systemicMetadata;
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
	
	/*@Override
	public SystemicMetadata generateIdIfAbsentFromDocument(SystemicMetadata systemicMetadata) {
		*//*if (!documentHasId(systemicMetadata)) {
			systemicMetadata.setId(new ObjectId().toString());
		}
		return systemicMetadata;*//*
		return null;
	}
	@Override
	public boolean documentHasId(SystemicMetadata systemicMetadata) {
		*//*return systemicMetadata.getId() != null;*//*
		return false;
	}
	@Override
	public BsonValue getDocumentId(SystemicMetadata systemicMetadata) {
	    *//*if (!documentHasId(systemicMetadata))
	    {
	        throw new IllegalStateException("The systemic metadata do not contain an _id");
	    }
	    return new BsonString(systemicMetadata.getId());*//*
		return null;
	}*/
}
