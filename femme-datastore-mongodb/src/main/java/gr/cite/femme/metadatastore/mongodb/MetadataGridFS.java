package gr.cite.femme.metadatastore.mongodb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.xpath.XPathFactoryConfigurationException;

import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.model.Updates;
import gr.cite.commons.utils.hash.ChecksumGeneratorUtils;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.model.SystemicMetadata;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.model.Status;
import gr.cite.femme.utils.Pair;
import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;
import gr.cite.scarabaues.utils.xml.exceptions.XMLConversionException;
import gr.cite.scarabaues.utils.xml.exceptions.XPathEvaluationException;

public class MetadataGridFS implements MongoMetadataCollection {
	private static final Logger logger = LoggerFactory.getLogger(MetadataGridFS.class);
	
	private GridFSBucket gridFSBucket;
	private MongoCollection<MetadataGridFSFile> gridFsFilesCollection;
	
	MetadataGridFS(GridFSBucket gridFSBucket, MongoCollection<MetadataGridFSFile> gridFsFilesCollection) {
		this.gridFSBucket = gridFSBucket;
		this.gridFsFilesCollection = gridFsFilesCollection;
	}

	private void removeWhiteSpaceAndCalculateChecksum(Metadatum metadatum) {
		if (metadatum.getContentType().contains("xml")) {
			metadatum.setValue(metadatum.getValue().replaceAll(">\\s+<", "><").trim());
		} else {
			metadatum.setValue(metadatum.getValue());
		}

		try {
			metadatum.setChecksum(ChecksumGeneratorUtils.generateMD5(metadatum.getValue()));
		} catch (HashGenerationException e) {
			logger.error("Checksum generation failed");
		}
	}

	@Override
	public void insert(Metadatum metadatum) throws MetadataStoreException {
		if (metadatum.getSystemicMetadata() == null) {
			metadatum.setSystemicMetadata(new SystemicMetadata());
		}
		Instant now = Instant.now();
		metadatum.getSystemicMetadata().setCreated(now);
		metadatum.getSystemicMetadata().setModified(now);
		metadatum.getSystemicMetadata().setStatus(Status.ACTIVE);

		removeWhiteSpaceAndCalculateChecksum(metadatum);

		this.upload(metadatum);
	}

	@Override
	public void update(Metadatum metadatum) throws MetadataStoreException {
		if (metadatum.getValue() != null) {
			removeWhiteSpaceAndCalculateChecksum(metadatum);

			MetadataGridFSFile metadataGridFSFile = this.gridFsFilesCollection
					.find(Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum())).limit(1).first();
			if (metadataGridFSFile == null) {
				insert(metadatum);
			}
		} else {
			this.gridFsFilesCollection.findOneAndUpdate(Filters.eq(Filters.eq(FieldNames.ID, new ObjectId(metadatum.getId()))),
					Updates.set(FieldNames.METADATA, metadatum));
		}
	}

	@Override
	public void updateStatus(String id, Status status) throws MetadataStoreException {
		this.gridFsFilesCollection.findOneAndUpdate(Filters.eq(Filters.eq(FieldNames.ID, new ObjectId(id))),
				Updates.set(FieldNames.METADATA + "." + FieldNames.STATUS, status.getStatusCode()));
	}

	private void upload(Metadatum metadatum) throws MetadataStoreException {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();

		InputStream streamToUploadFrom = new ByteArrayInputStream(metadatum.getValue().getBytes(StandardCharsets.UTF_8));
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(new Document()
					.append(FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()))
					.append(FieldNames.NAME, metadatum.getName())
					.append(FieldNames.CHECKSUM, metadatum.getChecksum())
					.append(FieldNames.CONTENT_TYPE, metadatum.getContentType())
					.append(FieldNames.STATUS, Status.ACTIVE.getStatusCode())
					.append(FieldNames.CREATED, Date.from(metadatum.getSystemicMetadata().getCreated()))
					.append(FieldNames.MODIFIED, Date.from(metadatum.getSystemicMetadata().getModified()))
					.append(FieldNames.STATUS, metadatum.getSystemicMetadata().getStatus().getStatusCode())
				);
		
		ObjectId fileId;
		try {
			fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum storage failed. Element id: " + metadatum.getElementId(), e);
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
			throw new MetadataStoreException("Metadatum retrieval failed. File id: " + fileId, e);
		}
		
		Metadatum metadatum = new Metadatum();
		metadatum.setValue(metadatumStream.toString());
		
		return metadatum;
	}

	@Override
	public MongoCursor<Metadatum> findAll(boolean lazy) throws MetadataStoreException {
		MongoCursor<Metadatum> cursor;
		try {
			cursor = this.gridFSBucket.find().map(gridFsFileToMetadatumTransformation(lazy)).iterator();
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return cursor;
	}

	@Override
	public MongoCursor<Metadatum> findAllBeforeTimestamp(Instant timestamp) throws MetadataStoreException {
		MongoCursor<Metadatum> cursor;
		try {
			cursor = this.gridFSBucket.find(Filters.lte(FieldNames.METADATA + "." + FieldNames.MODIFIED, Date.from(timestamp)))
					.map(gridFsFileToMetadatumTransformation(false)).iterator();
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return cursor;
	}

	@Override
	public List<Metadatum> find(String elementId) throws MetadataStoreException {
		return find(elementId, true);
	}
	
	@Override
	public List<Metadatum> find(String elementId, boolean lazy) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		try {
			this.gridFSBucket.find(Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
					.map(gridFsFileToMetadatumTransformation(lazy)).into(metadata);
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return metadata;
	}
	
	@Override
	public List<String> xPath(Metadatum metadatum, String xPath) throws MetadataStoreException {
		try {
			return new XPathEvaluator(XMLConverter.stringToNode(download(metadatum.getId()).getValue())).evaluate(xPath);
		} catch (XPathFactoryConfigurationException | XPathEvaluationException | XMLConversionException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
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
				metadatumInfo = new Pair<>(file.getObjectId(), file.getFilename());
			}
		} catch(MongoGridFSException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		} finally {
			cursor.close();
		}
		return metadatumInfo;
	}
	
	public boolean exists(Metadatum metadatum) {
		return this.gridFSBucket.find(buildMetadataFromDocument(metadatum)).filter(Projections.include("_id")).limit(1) != null;
	}
	
	@Override
	public void delete(Metadatum metadatum) {
		gridFSBucket.delete(new ObjectId(metadatum.getId()));
	}
	
	@Override
	public void deleteAll(String elementId) throws MetadataStoreException {
		try (MongoCursor<GridFSFile> cursor = this.gridFSBucket.find(
				Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
				.iterator()) {
			while (cursor.hasNext()) {
				this.gridFSBucket.delete(cursor.next().getObjectId());
			}
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Error while deleting metadatum from GridFS", e);
		}
	}

	private Function<GridFSFile, Metadatum> gridFsFileToMetadatumTransformation(boolean lazy) {
		return gridFSMetadatum -> {
			Metadatum metadatum = new Metadatum();

			metadatum.setId(gridFSMetadatum.getObjectId().toString());
			metadatum.setElementId(gridFSMetadatum.getMetadata().getObjectId(FieldNames.METADATA_ELEMENT_ID).toString());
			metadatum.setName(gridFSMetadatum.getMetadata().getString(FieldNames.NAME));
			metadatum.setContentType(gridFSMetadatum.getMetadata().getString(FieldNames.CONTENT_TYPE));
			metadatum.setChecksum(gridFSMetadatum.getMetadata().getString(FieldNames.CHECKSUM));

			SystemicMetadata systemicMetadata = new SystemicMetadata();
			systemicMetadata.setCreated(gridFSMetadatum.getMetadata().getDate(FieldNames.CREATED).toInstant());
			systemicMetadata.setModified(gridFSMetadatum.getMetadata().getDate(FieldNames.MODIFIED).toInstant());
			systemicMetadata.setStatus(Status.getEnum(gridFSMetadatum.getMetadata().getInteger(FieldNames.STATUS)));
			metadatum.setSystemicMetadata(systemicMetadata);

			if (!lazy) {
				try {
					metadatum.setValue(download(gridFSMetadatum.getObjectId().toString()).getValue());
				} catch (MetadataStoreException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			return metadatum;
		};
	}
	
	private Document buildMetadataFromDocument(Metadatum metadatum) {
		Document metadataDocument = new Document();
		if (metadatum.getElementId() != null) {
			metadataDocument.append(FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()));
		}
		if (metadatum.getName() != null) {
			metadataDocument.append(FieldNames.NAME, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			metadataDocument.append(FieldNames.CONTENT_TYPE, metadatum.getContentType());
		}
		System.out.println(new Document(FieldNames.METADATA, metadataDocument).toJson());
		return new Document(FieldNames.METADATA, metadataDocument);
	}
}
