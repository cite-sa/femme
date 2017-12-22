package gr.cite.femme.geo.mongodb.codecs;

import gr.cite.femme.geo.core.ServerGeo;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class ServerGeoCodec implements CollectibleCodec<ServerGeo> {
	private CodecRegistry codecRegistry;
	
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
	public ServerGeo decode(BsonReader bsonReader, DecoderContext decoderContext) {
		return null;
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, ServerGeo serverGeo, EncoderContext encoderContext) {
	
	}
	
	@Override
	public Class<ServerGeo> getEncoderClass() {
		return null;
	}
}
