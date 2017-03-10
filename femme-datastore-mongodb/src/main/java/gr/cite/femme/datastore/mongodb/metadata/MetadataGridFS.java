package gr.cite.femme.datastore.mongodb.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;
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

import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.cache.XPathCacheManager;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.model.Status;
import gr.cite.femme.utils.Pair;
import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;
import gr.cite.scarabaues.utils.xml.exceptions.XMLConversionException;
import gr.cite.scarabaues.utils.xml.exceptions.XPathEvaluationException;

public class MetadataGridFS implements MongoMetadataCollection {
	private static final Logger logger = LoggerFactory.getLogger(MetadataGridFS.class);
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	private static final String METADATUM_METADATA_KEY = "metadata";
	private static final String METADATUM_STATUS_KEY = "status";
	private static final String METADATUM_METADATA_ELEMENT_ID_PATH = METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY;
	
	private GridFSBucket gridFSBucket;
	
	public MetadataGridFS() {
		
	}
	
	public MetadataGridFS(GridFSBucket gridFSBucket) {
		this.gridFSBucket = gridFSBucket;
	}
	
	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException {
		this.upload(metadatum);
	}
	
	private void upload(Metadatum metadatum) throws MetadataStoreException {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();
		
		String value;
		if (metadatum.getContentType().contains("xml")) {
			value = metadatum.getValue().replaceAll(">\\s+<", "><").trim();
		} else {
			value = metadatum.getValue();
		}
		
		InputStream streamToUploadFrom = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
		
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(
					new Document()
					.append(FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()))
					.append(FieldNames.NAME, metadatum.getName())
					.append(FieldNames.METADATA_CONTENT_TYPE, metadatum.getContentType())
					.append(FieldNames.STATUS, Status.PENDING.getStatusCode())
				);
		
		ObjectId fileId;
		try {
			fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum storage failed. Element id: " + metadatum.getElementId().toString(), e);
		}
		metadatum.setId(fileId.toString());
		
	}
	
	@Override
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException {
		return download(metadatum.getId());
	}
	
	private Metadatum download(String fileId) throws MetadataStoreException {
		OutputStream metadatumStream = new ByteArrayOutputStream();
		try {
			gridFSBucket.downloadToStream(new ObjectId(fileId), metadatumStream);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum retrieval failed. File id: " + fileId.toString(), e);
		}
		
		Metadatum metadatum = new Metadatum();
		metadatum.setValue(metadatumStream.toString());
		
		return metadatum;
	}
	
	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return find(elementId, true);
	}
	
	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		
		Metadatum metadatum = new Metadatum();
		metadatum.setElementId(elementId);
		
		MongoCursor<Metadatum> cursor;
		try {
			cursor = gridFSBucket.find(
					new Document().append(METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY, new ObjectId(elementId)))
					.map(t -> {
                        Metadatum metadatum1 = new Metadatum();
                        metadatum1.setId(t.getObjectId().toString());
                        metadatum1.setElementId(t.getMetadata().getObjectId(METADATUM_ELEMENT_ID_KEY).toString());
                        metadatum1.setName(t.getMetadata().getString(METADATUM_NAME_KEY));
                        metadatum1.setContentType(t.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
                        if (!lazy) {
                            try {
                                metadatum1.setValue(download(t.getObjectId().toString()).getValue());
                            } catch (MetadataStoreException e) {
                                logger.error(e.getMessage(), e);
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                        return metadatum1;
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
	
	@Override
	public List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		Metadatum downloadedMetadatum = download(metadatum.getId());

		try {
			List<String> xPathResult = new XPathEvaluator(XMLConverter.stringToNode(downloadedMetadatum.getValue())).evaluate(xPath);

			/*metadatum.setValue(downloadedMetadatum.getValue());*/
			return xPathResult;
			
		} catch (XPathFactoryConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (XPathEvaluationException e1) {
			logger.error(e1.getMessage(), e1);
			throw new RuntimeException(e1.getMessage(), e1);
		} catch (XMLConversionException e2) {
			logger.error(e2.getMessage(), e2);
			throw new RuntimeException(e2.getMessage(), e2);
		}
	}
	
	/*public boolean xPath(List<Metadatum> metadata, String xPath) throws XPathFactoryConfigurationException {
		for (Metadatum metadatum: metadata) {
			if (new XPathEvaluator(XMLConverter.stringToNode(metadatum.getValue())).evaluate(xPath).size() > 0) {
				return true;
			}
		}
		return false;
	}*/
	
	
	/*public <T extends Element> T xPath(T element, String xPath) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		
		Metadatum metadatum = new Metadatum();
		metadatum.setElementId(elementId);
		
		MongoCursor<GridFSFile> cursor = null;
		try {
			cursor = gridFSBucket.query(
					new Document().append(METADATUM_METADATA_KEY + "." + METADATUM_ELEMENT_ID_KEY, new ObjectId(element.getId()))
					).iterator();
			map(new Function<GridFSFile, Metadatum>() {
				@Override
				public Metadatum apply(GridFSFile t) {
					Metadatum metadatum = download(t.getObjectId().toString());
					
					if (xPath(metadatum, xPath) != null) {
						metadatum.setElementId(element.getId());
						metadatum.setElementId(t.getMetadata().getObjectId(METADATUM_ELEMENT_ID_KEY).toString());
						metadatum.setName(t.getMetadata().getString(METADATUM_NAME_KEY));
						metadatum.setContentType(t.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
						try {
							metadatum.setValue(download(t.getObjectId().toString()).getValue());
						} catch (MetadataStoreException e) {
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e.getMessage(), e);
						}
						return metadatum;						
					}
					
				}
			}).iterator();
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		try {
			while (cursor.hasNext()) {
				GridFSFile file = cursor.next();
				Metadatum metadatum = download(file.getObjectId().toString());
				
				if (xPath(metadatum, xPath) != null) {
					metadatum.setElementId(element.getId());
					metadatum.setElementId(file.getMetadata().getObjectId(METADATUM_ELEMENT_ID_KEY).toString());
					metadatum.setName(file.getMetadata().getString(METADATUM_NAME_KEY));
					metadatum.setContentType(file.getMetadata().getString(METADATUM_CONTENT_TYPE_KEY));
					
					metadata.add(metadatum);
				}
			}
		} finally {
			cursor.close();
		}
		
		element.setMetadata(metadata);
		return element;
	}*/

	
	/*public <T extends Element> T query(T element, String xPath) throws MetadataStoreException {
		try {
			List<Metadatum> metadata = query(element.getId());
			if (xPath(metadata, xPath)) {
				element.setMetadata(metadata);
				return element;
			} else {
				return null;
			}
		} catch (XPathFactoryConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new MetadataStoreException("XPath on element with id: " + element.getId() + " failed", e);
		}
		try {
			return query(element.getId(), false).stream().filter(new Predicate<Metadatum>() {
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
	}*/
	
	/*public <T extends Element> List<T> query(List<T> elementIds, String xPath) throws MetadataStoreException {
		List<T> elements = null;
		try {
			elements = elementIds.stream().filter(new Predicate<T>() {
				@Override
				public boolean test(T element) {
					List<Metadatum> metadata = null;
					try {
						metadata = query(element.getId(), false);
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
	}*/
	
	/*public List<Metadatum> query(Metadatum metadatum) {
		List<Metadatum> metadata = new ArrayList<>();
		MongoCursor<Metadatum> cursor = gridFSBucket
				.query(buildMetadataFromDocument(metadatum)).map(new Function<GridFSFile, Metadatum>() {
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
	
	public List<Metadatum> query(List<Metadatum> metadataList) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		for (Metadatum metadatum : metadataList) {
			query(metadatum).stream().collect(Collectors.toCollection(() -> metadata));
		}
		
		return metadata;
	}*/
	
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
	
	@Override
	public void delete(Metadatum metadatum) {
		gridFSBucket.delete(new ObjectId(metadatum.getId()));
	}
	
	@Override
	public void deleteAll(String elementId) throws MetadataStoreException {
		MongoCursor<GridFSFile> cursor = gridFSBucket
				.find(Filters.eq(METADATUM_METADATA_ELEMENT_ID_PATH, new ObjectId(elementId))).iterator();
		try {
			while (cursor.hasNext()) {
				gridFSBucket.delete(cursor.next().getObjectId());
			}
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Error while deleting metadatum from GridFs", e);
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
