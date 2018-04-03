package gr.cite.femme.geo.mongodb.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.geo.ServerGeo;
import org.bson.*;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.time.Instant;

public class ServerGeoCodec implements CollectibleCodec<ServerGeo> {

	private CodecRegistry codecRegistry;
    static private final ObjectMapper mapper = new ObjectMapper();


    public ServerGeoCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public ServerGeo generateIdIfAbsentFromDocument(ServerGeo serverGeo) {
		return null;
	}
	
	@Override
	public boolean documentHasId(ServerGeo serverGeo) {
		return false;
	}
	
	@Override
	public BsonValue getDocumentId(ServerGeo serverGeo) {
		return null;
	}
	
	@Override
	public ServerGeo decode(BsonReader reader, DecoderContext decoderContext) {
        String id = null, collectionId = null;
        Instant created = null, modified = null;
        //Map<String, Object> geo = null;
        reader.readStartDocument();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals("_id")) {
                id = reader.readObjectId().toString();
            }
            else if (fieldName.equals("collectionId")){
                collectionId = reader.readString();
            }
            else if (fieldName.equals("created")) {
                created = Instant.ofEpochMilli(reader.readDateTime());
            } else if (fieldName.equals("modified")) {
                modified = Instant.ofEpochMilli(reader.readDateTime());
            }
        }
        reader.readEndDocument();
        ServerGeo serverGeo = new ServerGeo();
        serverGeo.setId(id);
        serverGeo.setCollectionId(collectionId);
        serverGeo.setCreated(created);
        serverGeo.setModified(modified);

        return serverGeo;
	}
	
	@Override
	public void encode(BsonWriter writer, ServerGeo serverGeo, EncoderContext encoderContext) {
        writer.writeStartDocument();

        if (!documentHasId(serverGeo)) {
            generateIdIfAbsentFromDocument(serverGeo);
        }
        if (serverGeo.getId() != null) {
            writer.writeObjectId("_id", new ObjectId(serverGeo.getId()));
        }
        else if(serverGeo.getCollectionId() != null ){
            writer.writeString("coverageId",serverGeo.getCollectionId());
        }
        if (serverGeo.getCreated() != null) {
            writer.writeDateTime("created", serverGeo.getCreated().toEpochMilli());
        }
        if (serverGeo.getModified() != null) {
            writer.writeDateTime("modified", serverGeo.getModified().toEpochMilli());
        }

        writer.writeEndDocument();
	}
	
	@Override
	public Class<ServerGeo> getEncoderClass() {
		return null;
	}
}
