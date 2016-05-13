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
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
import gr.cite.femme.datastore.mongodb.utils.MetadatumInfo;
import gr.cite.femme.utils.Pair;

public class MetadatumCodec implements CollectibleCodec<Metadatum> {
	private static final Logger logger = LoggerFactory.getLogger(MetadatumCodec.class);

	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";

	@Override
	public void encode(BsonWriter writer, Metadatum value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		if (value.getId() != null) {
			writer.writeObjectId(METADATUM_ID_KEY, new ObjectId(value.getId()));
			/*writer.writeObjectId(METADATUM_FILE_ID_KEY, new ObjectId(value.getId()));*/			
		}
		
		
		/* writer.writeString(METADATUM_FILENAME_KEY, file.getSecond()); */
		if (value.getName() != null) {
			writer.writeString(METADATUM_NAME_KEY, value.getName());
		}
		if (value.getContentType() != null) {
			writer.writeString(METADATUM_CONTENT_TYPE_KEY, value.getContentType());
		}

		writer.writeEndDocument();
	}

	@Override
	public Class<Metadatum> getEncoderClass() {
		return Metadatum.class;
	}

	@Override
	public Metadatum decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartDocument();

		String id = reader.readObjectId(METADATUM_ID_KEY).toString();
		/*ObjectId fileId = reader.readObjectId(METADATUM_FILE_ID_KEY);*/
		/* String fileName = reader.readString(METADATUM_FILENAME_KEY); */
		String name = reader.readString(METADATUM_NAME_KEY);
		String contentType = reader.readString(METADATUM_CONTENT_TYPE_KEY);

		reader.readEndDocument();

		/*
		 * Metadatum metadatum = metadatumGridFS.download(fileId);
		 * metadatum.setId(id); metadatum.setName(name);
		 * metadatum.setContentType(contentType);
		 */

		Metadatum metadatum = new Metadatum();
		metadatum.setId(id.toString());
		metadatum.setName(name);
		metadatum.setContentType(contentType);

		return metadatum;
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
	public BsonValue getDocumentId(Metadatum metadatum) {
		if (!documentHasId(metadatum)) {
			throw new IllegalStateException("The metadatum does not contain an _id");
		}
		return new BsonString(metadatum.getId());
	}
}
