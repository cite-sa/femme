package gr.cite.femme.geo.mongodb.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.geo.ServerGeo;
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

import java.time.Instant;

public class ServerGeoCodec implements CollectibleCodec<ServerGeo> {
	public static final String ID = "_id";
	public static final String COLLECTION_ID = "collectionId";
	public static final String SERVER_NAME = "serverName";
	public static final String CREATED = "created";
	public static final String MODIFIED = "modified";
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private CodecRegistry codecRegistry;
	
	public ServerGeoCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public ServerGeo generateIdIfAbsentFromDocument(ServerGeo serverGeo) {
		if (! documentHasId(serverGeo)) {
			serverGeo.setId(new ObjectId().toString());
		}
		return serverGeo;
	}
	
	@Override
	public boolean documentHasId(ServerGeo serverGeo) {
		return serverGeo.getId() != null;
	}
	
	@Override
	public BsonValue getDocumentId(ServerGeo serverGeo) {
		if (! documentHasId(serverGeo)) {
			throw new IllegalStateException("ServerGeo does not contain an _id");
		}
		return new BsonString(serverGeo.getId());
	}
	
	@Override
	public ServerGeo decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, collectionId = null, serverName = null;
		Instant created = null, modified = null;
		
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			
			if (fieldName.equals(ID)) {
				id = reader.readObjectId().toString();
			} else if (fieldName.equals(COLLECTION_ID)) {
				collectionId = reader.readObjectId().toString();
			} else if (fieldName.equals(SERVER_NAME)) {
				serverName = reader.readString();
			} else if (fieldName.equals(CREATED)) {
				created = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals(MODIFIED)) {
				modified = Instant.ofEpochMilli(reader.readDateTime());
			}
		}
		reader.readEndDocument();
		
		ServerGeo server = new ServerGeo();
		server.setId(id);
		server.setCollectionId(collectionId);
		server.setServerName(serverName);
		server.setCreated(created);
		server.setModified(modified);
		
		return server;
	}
	
	@Override
	public void encode(BsonWriter writer, ServerGeo server, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (!documentHasId(server)) {
			generateIdIfAbsentFromDocument(server);
		}
		
		if (server.getId() != null) {
			writer.writeObjectId(ID, new ObjectId(server.getId()));
		}
		if (server.getCollectionId() != null) {
			writer.writeObjectId(COLLECTION_ID, new ObjectId(server.getCollectionId()));
		}
		if (server.getServerName() != null) {
			writer.writeString(SERVER_NAME, server.getServerName());
		}
		if (server.getCreated() != null) {
			writer.writeDateTime(CREATED, server.getCreated().toEpochMilli());
		}
		if (server.getModified() != null) {
			writer.writeDateTime(MODIFIED, server.getModified().toEpochMilli());
		}
		
		writer.writeEndDocument();
	}
	
	@Override
	public Class<ServerGeo> getEncoderClass() {
		return ServerGeo.class;
	}
}
