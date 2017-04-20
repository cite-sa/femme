package gr.cite.femme.engine.metadatastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.engine.metadatastore.mongodb.MetadataGridFSFile;
import gr.cite.femme.core.model.Metadatum;
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

public class MetadataGridFSFileCodec implements CollectibleCodec<MetadataGridFSFile> {
	private static final String ID = "_id";
	private static final String FILENAME = "filename";
	private static final String LENGTH = "length";
	private static final String CHUNK_SIZE = "chunkSize";
	private static final String UPLOAD_DATE = "uploadDate";
	private static final String MD5 = "md5";
	private static final String METADATA = "metadata";

	private CodecRegistry codecRegistry;

	public MetadataGridFSFileCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, MetadataGridFSFile value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}

		if (value.getId() != null) {
			writer.writeObjectId(MetadataGridFSFileCodec.ID, new ObjectId(value.getId()));
		}
		if (value.getFilename() != null) {
			writer.writeString(MetadataGridFSFileCodec.FILENAME, value.getFilename());
		}
		if (value.getLength() != null) {
			writer.writeInt64(MetadataGridFSFileCodec.LENGTH, value.getLength());
		}
		if (value.getChunkSize() != null) {
			writer.writeInt32(MetadataGridFSFileCodec.CHUNK_SIZE, value.getChunkSize());
		}
		if (value.getUploadDate() != null) {
			writer.writeDateTime(MetadataGridFSFileCodec.UPLOAD_DATE, value.getUploadDate().toEpochMilli());
		}
		if (value.getMd5()!= null) {
			writer.writeString(MetadataGridFSFileCodec.MD5, value.getMd5());
		}
		if (value.getMetadata() != null) {
			writer.writeName(MetadataGridFSFileCodec.METADATA);
			encoderContext.encodeWithChildContext(this.codecRegistry.get(Metadatum.class), writer, value.getMetadata());
		}

		writer.writeEndDocument();
	}

	@Override
	public Class<MetadataGridFSFile> getEncoderClass() {
		return MetadataGridFSFile.class;
	}

	@Override
	public MetadataGridFSFile decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, filename = null, md5 = null;
		Long length = null;
		Integer chunkSize = null;
		Instant uploadDate = null;
		Metadatum metadata = null;

		reader.readStartDocument();

		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();

			if (fieldName.equals(FieldNames.ID)) {
				id = reader.readObjectId().toString();
			} else if (fieldName.equals(MetadataGridFSFileCodec.FILENAME)) {
				filename = reader.readString();
			} else if (fieldName.equals(MetadataGridFSFileCodec.LENGTH)) {
				length = reader.readInt64();
			} else if (fieldName.equals(MetadataGridFSFileCodec.CHUNK_SIZE)) {
				chunkSize = reader.readInt32();
			} else if (fieldName.equals(MetadataGridFSFileCodec.UPLOAD_DATE)) {
				uploadDate = Instant.ofEpochMilli(reader.readDateTime());
			} else if (fieldName.equals(MetadataGridFSFileCodec.MD5)) {
				md5 = reader.readString();
			} else if (fieldName.equals(MetadataGridFSFileCodec.METADATA)) {
				if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
					metadata = this.codecRegistry.get(Metadatum.class).decode(reader, decoderContext);
				}
			}
		}

		reader.readEndDocument();

		MetadataGridFSFile metadataGridFSFile = new MetadataGridFSFile();
		metadataGridFSFile.setId(id);
		metadataGridFSFile.setFilename(filename);
		metadataGridFSFile.setLength(length);
		metadataGridFSFile.setChunkSize(chunkSize);
		metadataGridFSFile.setUploadDate(uploadDate);
		metadataGridFSFile.setMd5(md5);

		if (metadata != null) {
			metadata.setId(id);
		}
		metadataGridFSFile.setMetadata(metadata);

		return metadataGridFSFile;
	}

	@Override
	public MetadataGridFSFile generateIdIfAbsentFromDocument(MetadataGridFSFile metadataGridFSFile) {
		if (!documentHasId(metadataGridFSFile)) {
			metadataGridFSFile.setId(new ObjectId().toString());
		}
		return metadataGridFSFile;
	}

	@Override
	public boolean documentHasId(MetadataGridFSFile metadataGridFSFile) {
		return metadataGridFSFile.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(MetadataGridFSFile metadatum) {
		if (!documentHasId(metadatum)) {
			throw new IllegalStateException("The MetadataGridFS file does not contain an _id");
		}
		return new BsonString(metadatum.getId());
	}
}
