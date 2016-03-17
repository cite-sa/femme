package gr.cite.femme.datastore.mongodb.gridfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Function;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Metadatum;
import gr.cite.femme.utils.Pair;

public class MetadatumGridFS {
	private static final Logger logger = LoggerFactory.getLogger(MetadatumGridFS.class);
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	private static final String METADATUM_METADATA_KEY = "metadata";
	private static final String METADATUM_METADATA_ELEMENT_ID_PATH = METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY;
	
	private GridFSBucket gridFSBucket;
	
	public MetadatumGridFS() {
		
	}
	public MetadatumGridFS(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}
	
	public Pair<ObjectId, String> upload(Metadatum metadatum) {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();
		InputStream streamToUploadFrom = new ByteArrayInputStream(
				metadatum.getValue().getBytes(StandardCharsets.UTF_8));
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(
					new Document()
					.append(METADATUM_ELEMENT_ID_KEY, new ObjectId(metadatum.getElementId()))
					.append(METADATUM_NAME_KEY, metadatum.getName())
					.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType())
				);

		ObjectId fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		
		return new Pair<ObjectId, String>(fileId, filename);
	}
	
	public Metadatum download(ObjectId fileId) {
		OutputStream metadatumStream = new ByteArrayOutputStream();
		gridFSBucket.downloadToStream(fileId, metadatumStream);
		
		Metadatum metadatum = new Metadatum();
		metadatum.setValue(metadatumStream.toString());
		
		return metadatum;
	}
	
	public List<Metadatum> find(Metadatum metadatum) {
		List<Metadatum> metadata = new ArrayList<>();
		MongoCursor<Metadatum> cursor = gridFSBucket
				.find(buildMetadataFromDocument(metadatum)).map(new Function<GridFSFile, Metadatum>() {
					@Override
					public Metadatum apply(GridFSFile t) {
						Metadatum metadatum = new Metadatum();
						metadatum.setElementId(t.getMetadata().getObjectId(METADATUM_METADATA_KEY).toString());
						metadatum.setName(t.getMetadata().getString(METADATUM_NAME_KEY));
						metadatum.setContentType(t.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
						return metadatum;
					}
				}).iterator();
		try {
			while (cursor.hasNext()) {
				metadata.add(cursor.next());
			}
		} catch(MongoGridFSException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			cursor.close();
		}
		return metadata;
	}
	
	public void delete(String elementId) {
		MongoCursor<GridFSFile> cursor = gridFSBucket
				.find(Filters.eq(METADATUM_METADATA_ELEMENT_ID_PATH, new ObjectId(elementId))).iterator();
		try {
			while (cursor.hasNext()) {
				gridFSBucket.delete(cursor.next().getObjectId());
			}
		} catch(MongoGridFSException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			cursor.close();
		}
	}
	
	private Document buildMetadataFromDocument(Metadatum metadatum) {
		Document metadataDocument = new Document();
		if (metadatum.getElementId() != null) {
			metadataDocument.append(METADATUM_ELEMENT_ID_KEY, metadatum.getElementId());
		}
		if (metadatum.getName() != null) {
			metadataDocument.append(METADATUM_NAME_KEY, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			metadataDocument.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
		}
		return new Document(METADATUM_METADATA_KEY, metadataDocument);
	}
}
