package gr.cite.femme.geo.mongodb.codecs;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.geo.core.CoverageGeo;
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

import gr.cite.femme.core.model.BBox;
import org.bson.types.ObjectId;
/*import DateTime;*/

public class CoverageGeoCodec implements CollectibleCodec<CoverageGeo> {
	static private final ObjectMapper mapper = new ObjectMapper();
	private CodecRegistry codecRegistry;
	
	public CoverageGeoCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public void encode(BsonWriter writer, CoverageGeo coverageGeo, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (!documentHasId(coverageGeo)) {
			generateIdIfAbsentFromDocument(coverageGeo);
		}
		if (coverageGeo.getId() != null) {
			writer.writeObjectId("_id", new ObjectId(coverageGeo.getId()));
		}
		if (coverageGeo.getCreated() != null) {
			writer.writeDateTime("created", coverageGeo.getCreated().toEpochMilli());
		}
		if (coverageGeo.getModified() != null) {
			writer.writeDateTime("modified", coverageGeo.getModified().toEpochMilli());
		}
		if (coverageGeo.getServerId() != null) {
			writer.writeObjectId("serverId", new ObjectId(coverageGeo.getServerId()));
		}
		if (coverageGeo.getGeo() != null && coverageGeo.getGeo().size() > 0) {
			writer.writeName("geo");
			writer.writeStartDocument();
			
			for (final Map.Entry<String, String> entry : coverageGeo.getGeo().entrySet()) {
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
	public Class<CoverageGeo> getEncoderClass() {
		return CoverageGeo.class;
	}
	
	@Override
	public CoverageGeo decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, serverId = null;
		Instant created = null, modified = null;
		Map<String, String> geo = null;
		
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
		
			if (fieldName.equals("_id")) {
	        	id = reader.readObjectId().toString();
	        } else if (fieldName.equals("created")) {
				created = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals("modified")) {
				modified = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals("serverId")) {
				serverId = reader.readObjectId().toString();
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
			}
		}
		
		reader.readEndDocument();
		
		CoverageGeo coverageGeo = new CoverageGeo();
		coverageGeo.setId(id);
		coverageGeo.setCreated(created);
		coverageGeo.setModified(modified);
		coverageGeo.setServerId(serverId);
		coverageGeo.setGeo(geo);
		
		return coverageGeo;
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
	public CoverageGeo generateIdIfAbsentFromDocument(CoverageGeo coverageGeo) {
		if (!documentHasId(coverageGeo)) {
			coverageGeo.setId(new ObjectId().toString());
		}
		return coverageGeo;
	}
	
	@Override
	public boolean documentHasId(CoverageGeo coverageGeo) {
		return coverageGeo.getId() != null;
	}
	@Override
	public BsonValue getDocumentId(CoverageGeo coverageGeo) {
	    if (!documentHasId(coverageGeo)) {
	        throw new IllegalStateException("The coverage does not contain an _id");
	    }
	    return new BsonString(coverageGeo.getId());
	}
	
}
