package gr.cite.femme.datastore.mongodb.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathFactoryConfigurationException;

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
import com.mongodb.client.model.Projections;

import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.utils.Pair;
import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;

public class MetadataGridFS implements MetadataStore {
	private static final Logger logger = LoggerFactory.getLogger(MetadataGridFS.class);
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	private static final String METADATUM_METADATA_KEY = "metadata";
	private static final String METADATUM_METADATA_ELEMENT_ID_PATH = METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY;
	
	private GridFSBucket gridFSBucket;
	
	public MetadataGridFS() {
		
	}
	
	public MetadataGridFS(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}
	
	public ObjectId insert(Metadatum metadatum, String elementId) throws MetadataStoreException {
		return this.upload(metadatum, elementId);
	}
	
	public ObjectId upload(Metadatum metadatum, String elementId) throws MetadataStoreException {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();
		InputStream streamToUploadFrom = new ByteArrayInputStream(
				metadatum.getValue().getBytes(StandardCharsets.UTF_8));
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(
					new Document()
					.append(METADATUM_ELEMENT_ID_KEY, new ObjectId(elementId))
					.append(METADATUM_NAME_KEY, metadatum.getName())
					.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType())
				);
		ObjectId fileId;
		try {
			fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("GridsFSException when uploading metadatum of element with id: " + elementId.toString(), e);
		}
		metadatum.setId(fileId.toString());
		
		return fileId;
	}
	
	public Metadatum get(String fileId) throws MetadataStoreException {
		return download(fileId);
	}
	
	public Metadatum download(String fileId) throws MetadataStoreException {
		OutputStream metadatumStream = new ByteArrayOutputStream();
		try {
			gridFSBucket.downloadToStream(new ObjectId(fileId), metadatumStream);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("GridsFSException when downloading metadatum with fileId: " + fileId.toString(), e);
		}
		
		Metadatum metadatum = new Metadatum();
		metadatum.setValue(metadatumStream.toString());
		
		return metadatum;
	}
	
	
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return find(elementId, true);
	}
	
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		
		Metadatum metadatum = new Metadatum();
		metadatum.setElementId(elementId);
		
		MongoCursor<Metadatum> cursor = null;
		try {
			cursor = gridFSBucket.find(
					new Document().append(METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY, new ObjectId(elementId))
					).map(new Function<GridFSFile, Metadatum>() {
				@Override
				public Metadatum apply(GridFSFile t) {
					Metadatum metadatum = new Metadatum();
					metadatum.setElementId(t.getMetadata().getObjectId(METADATUM_ELEMENT_ID_KEY).toString());
					metadatum.setName(t.getMetadata().getString(METADATUM_NAME_KEY));
					metadatum.setContentType(t.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
					if (!lazy) {
						try {
							metadatum.setValue(download(t.getObjectId().toString()).getValue());
						} catch (MetadataStoreException e) {
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return metadatum;
				}
			}).iterator();
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		try {
			while (cursor.hasNext()) {
				metadata.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return metadata;
	}
	
	public Metadatum xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		Metadatum downloadedMetadatum = download(metadatum.getId());
		try {
			if (new XPathEvaluator(XMLConverter.stringToNode(downloadedMetadatum.getValue())).evaluate(xPath).size() > 0) {
				return downloadedMetadatum;
			} else {
				return null;
			}
		} catch (XPathFactoryConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	
	public <T extends Element> List<Metadatum> find(T element, String xPath) throws MetadataStoreException {
		try {
			return find(element.getId(), false).stream().filter(new Predicate<Metadatum>() {
				@Override
				public boolean test(Metadatum metadatum) {
					try {
						if (new XPathEvaluator(XMLConverter.stringToNode(metadatum.getValue())).evaluate(xPath).size() > 0) {
							return true;
						}
					} catch (XPathFactoryConfigurationException e) {
						logger.error(e.getMessage(), e);
						throw new RuntimeException(e.getMessage(), e);
					}
					return false;
				}
			}).collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}
	
	public <T extends Element> List<T> find(List<T> elementIds, String xPath) throws MetadataStoreException {
		List<T> elements = null;
		try {
			elements = elementIds.stream().filter(new Predicate<T>() {
				@Override
				public boolean test(T element) {
					List<Metadatum> metadata = null;
					try {
						metadata = find(element.getId(), false);
					} catch (MetadataStoreException e) {
						logger.error(e.getMessage(), e);
						throw new RuntimeException(e.getMessage(), e);
					}
					for (Metadatum metadatum: metadata) {
						try {
							if (new XPathEvaluator(XMLConverter.stringToNode(metadatum.getValue())).evaluate(xPath).size() > 0) {
								return true;
							}
						} catch (XPathFactoryConfigurationException e) {
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return false;
				}
			}).collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return elements;
	}
	
	public List<Metadatum> find(Metadatum metadatum) {
		List<Metadatum> metadata = new ArrayList<>();
		MongoCursor<Metadatum> cursor = gridFSBucket
				.find(buildMetadataFromDocument(metadatum)).map(new Function<GridFSFile, Metadatum>() {
					@Override
					public Metadatum apply(GridFSFile t) {
						Metadatum metadatum = new Metadatum();
						metadatum.setElementId(t.getMetadata().getObjectId(METADATUM_ELEMENT_ID_KEY).toString());
						metadatum.setName(t.getMetadata().getString(METADATUM_NAME_KEY));
						metadatum.setContentType(t.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
						return metadatum;
					}
				}).iterator();
		try {
			while (cursor.hasNext()) {
				metadata.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		
		return metadata;
	}
	
	public List<Metadatum> find(List<Metadatum> metadataList) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		for (Metadatum metadatum : metadataList) {
			find(metadatum).stream().collect(Collectors.toCollection(() -> metadata));
		}
		
		return metadata;
	}
	
	public Pair<ObjectId, String> getMetadatumInfo(Metadatum metadatum) throws MetadataStoreException {
		Pair<ObjectId, String> metadatumInfo = null;
		MongoCursor<GridFSFile> cursor = gridFSBucket.find(buildMetadataFromDocument(metadatum))
				/*.filter(Projections.include(FILE_ID_KEY, FILE_FILENAME_KEY))*/.limit(1).iterator();
		try {
			while (cursor.hasNext()) {
				GridFSFile file = cursor.next();
				metadatumInfo = new Pair<ObjectId, String>(file.getObjectId(), file.getFilename());
			}
		} catch(MongoGridFSException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		} finally {
			cursor.close();
		}
		return metadatumInfo;
	}
	
	public boolean exists(Metadatum metadatum) {
		return gridFSBucket.find(buildMetadataFromDocument(metadatum)).filter(Projections.include("_id")).limit(1) != null;
	}
	
	public void delete(String elementId) {
		MongoCursor<GridFSFile> cursor = gridFSBucket
				.find(Filters.eq(METADATUM_METADATA_ELEMENT_ID_PATH, new ObjectId(elementId))).iterator();
		try {
			while (cursor.hasNext()) {
				gridFSBucket.delete(cursor.next().getObjectId());
			}
		} finally {
			cursor.close();
		}
	}
	
	private Document buildMetadataFromDocument(Metadatum metadatum) {
		Document metadataDocument = new Document();
		if (metadatum.getElementId() != null) {
			metadataDocument.append(METADATUM_ELEMENT_ID_KEY, new ObjectId(metadatum.getElementId()));
		}
		if (metadatum.getName() != null) {
			metadataDocument.append(METADATUM_NAME_KEY, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			metadataDocument.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
		}
		System.out.println(new Document(METADATUM_METADATA_KEY, metadataDocument).toJson());
		return new Document(METADATUM_METADATA_KEY, metadataDocument);
	}
}
