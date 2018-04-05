package gr.cite.femme.geo.mongodb.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.geo.CoverageGeo;
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
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

public class CoverageGeoCodec implements CollectibleCodec<CoverageGeo> {
	public static final String ID = "_id";
	public static final String COVERAGE_NAME = "coverageName";
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
	public static final String SERVER_ID = "serverId";
	public static final String DATA_ELEMENT_ID = "dataElementId";
	public static final String CRS = "crs";
	public static final String LOC = "loc";
	
	private static final Logger logger = LoggerFactory.getLogger(CoverageGeoCodec.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private CodecRegistry codecRegistry;
	
	public CoverageGeoCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public void encode(BsonWriter writer, CoverageGeo coverageGeo, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (! documentHasId(coverageGeo)) {
			generateIdIfAbsentFromDocument(coverageGeo);
		}
		
		if (coverageGeo.getId() != null) {
			writer.writeObjectId(ID, new ObjectId(coverageGeo.getId()));
		} else if (coverageGeo.getCoverageName() != null) {
			writer.writeString(COVERAGE_NAME, coverageGeo.getCoverageName());
		}
		if (coverageGeo.getCreated() != null) {
			writer.writeDateTime(CREATED, coverageGeo.getCreated().toEpochMilli());
		}
		if (coverageGeo.getModified() != null) {
			writer.writeDateTime(MODIFIED, coverageGeo.getModified().toEpochMilli());
		}
		if (coverageGeo.getDataElementId() != null) {
			writer.writeObjectId(DATA_ELEMENT_ID, new ObjectId(coverageGeo.getDataElementId()));
		}
		if (coverageGeo.getServerId() != null) {
			writer.writeObjectId(SERVER_ID, new ObjectId(coverageGeo.getServerId()));
		}
		if (coverageGeo.getCrs() != null) {
			writer.writeString(CRS, coverageGeo.getCrs());
		}
		if (coverageGeo.getGeo() != null) {
			writer.writeName(LOC);
			try {
				String json = mapper.writeValueAsString(coverageGeo.getGeo());
				encoderContext.encodeWithChildContext(this.codecRegistry.get(Document.class), writer, Document.parse(json));
			} catch (IOException e) {
				logger.error("Error encoding " + LOC + " [" + coverageGeo.getId() + "]", e.getMessage());
			}
		}
		writer.writeEndDocument();
	}
	
	@Override
	public Class<CoverageGeo> getEncoderClass() {
		return CoverageGeo.class;
	}
	
	@Override
	public CoverageGeo decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, coverageName = null, serverId = null, dataElementId = null, crs = null;
		Instant created = null, modified = null;
		GeoJsonObject geo = null;
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			
			if (fieldName.equals(ID)) {
				id = reader.readObjectId().toString();
			} else if (fieldName.equals(COVERAGE_NAME)) {
				coverageName = reader.readString();
			} else if (fieldName.equals(CREATED)) {
				created = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals(MODIFIED)) {
				modified = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals(DATA_ELEMENT_ID)) {
				dataElementId = reader.readObjectId().toString();
			} else if (fieldName.equals(SERVER_ID)) {
				serverId = reader.readObjectId().toString();
			} else if (fieldName.equals(CRS)) {
				crs = reader.readString();
			} else if (fieldName.equals(LOC)) {
				Document value = null;
				if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
					value = this.codecRegistry.get(Document.class).decode(reader, decoderContext);
				}
				
				try {
					geo = mapper.readValue(value.toJson(), GeoJsonObject.class);
				} catch (IOException e) {
					logger.error("Error decoding loc [" + id + "]", e.getMessage());
				}
			}
		}
		
		reader.readEndDocument();
		
		CoverageGeo coverageGeo = new CoverageGeo();
		coverageGeo.setId(id);
		coverageGeo.setCoverageName(coverageName);
		coverageGeo.setCreated(created);
		coverageGeo.setModified(modified);
		coverageGeo.setServerId(serverId);
		coverageGeo.setDataElementId(dataElementId);
		coverageGeo.setCrs(crs);
		coverageGeo.setGeo(geo);
		
		return coverageGeo;
	}
	
	@Override
	public CoverageGeo generateIdIfAbsentFromDocument(CoverageGeo coverageGeo) {
		if (! documentHasId(coverageGeo)) {
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
		if (! documentHasId(coverageGeo)) {
			throw new IllegalStateException("The coverage does not contain an _id");
		}
		return new BsonString(coverageGeo.getId());
	}
	
}
