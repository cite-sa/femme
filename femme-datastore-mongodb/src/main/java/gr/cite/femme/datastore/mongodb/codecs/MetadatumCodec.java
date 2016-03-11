package gr.cite.femme.datastore.mongodb.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.utils.Pair;

public class MetadatumCodec implements CollectibleCodec<Metadatum> {
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	
	private GridFSBucket gridFSBucket;
	
	public MetadatumCodec() {
	}
	
	public MetadatumCodec(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}
	
	@Override
	public void encode(BsonWriter writer, Metadatum value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		
		Pair<ObjectId, String> file = insertMetadata(value);
		
		writer.writeObjectId(METADATUM_ID_KEY, new ObjectId(value.getId()));
		writer.writeObjectId(METADATUM_FILE_ID_KEY, file.getFirst());
		writer.writeString(METADATUM_FILENAME_KEY, file.getSecond());
		writer.writeString(METADATUM_NAME_KEY, value.getName());
		writer.writeString(METADATUM_CONTENT_TYPE_KEY, value.getContentType());
		
		writer.writeEndDocument();
	}
	
	private Pair<ObjectId, String> insertMetadata(Metadatum metadatum) {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();
		InputStream streamToUploadFrom = new ByteArrayInputStream(
				metadatum.getValue().getBytes(StandardCharsets.UTF_8));
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(
					new Document()
					.append(METADATUM_NAME_KEY, metadatum.getName())
					.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType())
				);

		ObjectId fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		
		return new Pair<ObjectId, String>(fileId, filename);
	}
	@Override
	public Class<Metadatum> getEncoderClass() {
		return Metadatum.class;
	}
	@Override
	public Metadatum decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartDocument();
		
		String id = reader.readObjectId(METADATUM_ID_KEY).toString();
		ObjectId fileId = reader.readObjectId(METADATUM_FILE_ID_KEY);
		String fileName = reader.readString(METADATUM_FILENAME_KEY);
		String name = reader.readString(METADATUM_NAME_KEY);
		String contentType = reader.readString(METADATUM_CONTENT_TYPE_KEY);
		
		reader.readEndDocument();
		
		OutputStream metadatumStream = new ByteArrayOutputStream();
		gridFSBucket.downloadToStream(fileId, metadatumStream);
		
		return new Metadatum(id, name, metadatumStream.toString(), contentType);
	}
	@Override
	public Metadatum generateIdIfAbsentFromDocument(Metadatum metadatum) {
		if (!documentHasId(metadatum)) {
			metadatum.setId(new ObjectId().toString());
		}
		return metadatum;
	}
	@Override
	public boolean documentHasId(Metadatum metadatum) {
		return metadatum.getId() != null;
	}
	@Override
	public BsonValue getDocumentId(Metadatum metadatum)
	{
	    if (!documentHasId(metadatum))
	    {
	        throw new IllegalStateException("The metadatum does not contain an _id");
	    }
	    return new BsonString(metadatum.getId());
	}
}
