package gr.cite.femme.engine.metadatastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.model.Status;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.time.Instant;

public class MetadataGridFSFileMetadataCodec implements Codec<Metadatum> {

	private CodecRegistry codecRegistry;

	public MetadataGridFSFileMetadataCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Metadatum value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (value.getElementId() != null) {
			writer.writeObjectId(FieldNames.METADATA_ELEMENT_ID, new ObjectId(value.getElementId()));
		}
		if (value.getEndpoint() != null) {
			writer.writeString(FieldNames.ENDPOINT, value.getEndpoint());
		}
		if (value.getName() != null) {
			writer.writeString(FieldNames.NAME, value.getName());
		}
		if (value.getChecksum() != null) {
			writer.writeString(FieldNames.CHECKSUM, value.getChecksum());
		}
		if (value.getContentType() != null) {
			writer.writeString(FieldNames.CONTENT_TYPE, value.getContentType());
		}
		if (value.getSystemicMetadata() != null && value.getSystemicMetadata().getStatus() != null) {
			writer.writeInt32(FieldNames.STATUS, value.getSystemicMetadata().getStatus().getStatusCode());
		}
		if (value.getSystemicMetadata() != null && value.getSystemicMetadata().getCreated() != null) {
			writer.writeDateTime(FieldNames.CREATED, value.getSystemicMetadata().getCreated().toEpochMilli());
		}
		if (value.getSystemicMetadata() != null && value.getSystemicMetadata().getModified() != null) {
			writer.writeDateTime(FieldNames.MODIFIED, value.getSystemicMetadata().getModified().toEpochMilli());
		}

		writer.writeEndDocument();
	}

	@Override
	public Class<Metadatum> getEncoderClass() {
		return Metadatum.class;
	}

	@Override
	public Metadatum decode(BsonReader reader, DecoderContext decoderContext) {
		String elementId = null, endpoint = null, name = null, checksum = null, contentType = null;
		Status status = null;
		Instant created = null, modified = null;

		reader.readStartDocument();

		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();

			if (fieldName.equals(FieldNames.METADATA_ELEMENT_ID)) {
				elementId = reader.readObjectId().toString();
			} else if (fieldName.equals(FieldNames.ENDPOINT)) {
				endpoint = reader.readString();
			} else if (fieldName.equals(FieldNames.NAME)) {
				name = reader.readString();
			} else if (fieldName.equals(FieldNames.CHECKSUM)) {
				checksum = reader.readString();
			} else if (fieldName.equals(FieldNames.CONTENT_TYPE)) {
				contentType = reader.readString();
			} else if (fieldName.equals(FieldNames.CREATED)) {
				created = Instant.ofEpochMilli(reader.readDateTime());
			}  else if (fieldName.equals(FieldNames.MODIFIED)) {
				modified = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals(FieldNames.STATUS)) {
				status = Status.getEnum(reader.readInt32());
			}
		}

		reader.readEndDocument();

		Metadatum metadata = new Metadatum();
		metadata.setElementId(elementId);

		metadata.setName(name);
		metadata.setChecksum(checksum);
		metadata.setContentType(contentType);
		metadata.setEndpoint(endpoint);

		SystemicMetadata systemicMetadata = new SystemicMetadata();
		systemicMetadata.setStatus(status);
		systemicMetadata.setCreated(created);
		systemicMetadata.setModified(modified);

		metadata.setSystemicMetadata(systemicMetadata);

		return metadata;
	}
}