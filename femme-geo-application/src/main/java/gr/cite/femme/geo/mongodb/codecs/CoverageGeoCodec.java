package gr.cite.femme.geo.mongodb.codecs;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.model.Status;
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
import org.geojson.GeoJsonObject;
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
		else if(coverageGeo.getCoverageName() != null ){
			writer.writeString("coverageId",coverageGeo.getCoverageName());
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
		if (coverageGeo.getGeo() != null) {
			writer.writeName("loc");
			try {
				String json= new ObjectMapper().writeValueAsString(coverageGeo.getGeo());
				encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse(json));

			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		String id = null, serverId = null, coverageId = null;
		Instant created = null, modified = null;
		//Map<String, Object> geo = null;
		GeoJsonObject geo = null;
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
		
			if (fieldName.equals("_id")) {
	        	id = reader.readObjectId().toString();
	        }
	        else if (fieldName.equals("coverageId")){
				coverageId = reader.readString();
			}
	        else if (fieldName.equals("created")) {
				created = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals("modified")) {
				modified = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals("serverId")) {
				serverId = reader.readObjectId().toString();
			} else if (fieldName.equals("loc")) {

				Document value = null;
				if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
					value = this.codecRegistry.get(Document.class).decode(reader, decoderContext);
				}
				try {
					geo = mapper.readValue(value.toJson(), GeoJsonObject.class);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		
		reader.readEndDocument();
		
		CoverageGeo coverageGeo = new CoverageGeo();
		coverageGeo.setId(id);
		coverageGeo.setCoverageName(coverageId);
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
