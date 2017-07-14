package gr.cite.femme.engine.metadatastore.mongodb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import gr.cite.commons.utils.hash.HashGeneratorUtils;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.commons.utils.xml.XMLFormatter;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.SystemicMetadata;
import org.bson.Document;
import org.bson.conversions.Bson;
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

import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.utils.Pair;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;

public class MetadataGridFS implements MongoMetadataCollection {
	private static final Logger logger = LoggerFactory.getLogger(MetadataGridFS.class);
	
	private GridFSBucket gridFSBucket;
	private MongoCollection<MetadataGridFSFile> gridFsFilesCollection;
	
	MetadataGridFS(GridFSBucket gridFSBucket, MongoCollection<MetadataGridFSFile> gridFsFilesCollection) {
		this.gridFSBucket = gridFSBucket;
		this.gridFsFilesCollection = gridFsFilesCollection;
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

		try {
			removeWhiteSpaceAndCalculateChecksum(metadatum);
		} catch (HashGenerationException e) {
			throw new MetadataStoreException(e);
		}

		this.upload(metadatum);
	}

	private void upload(Metadatum metadatum) throws MetadataStoreException {
		String filename = metadatum.getName() + "_" + UUID.randomUUID().toString();

		InputStream streamToUploadFrom = new ByteArrayInputStream(metadatum.getValue().getBytes(StandardCharsets.UTF_8));
		Document metadata = new Document();
		if (metadatum.getElementId() != null) {
			metadata.append(FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()));
		}
		if (metadatum.getEndpoint() != null) {
			metadata.append(FieldNames.ENDPOINT, metadatum.getEndpoint());
		}
		if (metadatum.getName() != null) {
			metadata.append(FieldNames.NAME, metadatum.getName());
		}
		if (metadatum.getChecksum() != null) {
			metadata.append(FieldNames.CHECKSUM, metadatum.getChecksum());
		}
		if (metadatum.getContentType() != null) {
			metadata.append(FieldNames.CONTENT_TYPE, metadatum.getContentType());
		}
		if (metadatum.getSystemicMetadata() != null) {
			if (metadatum.getSystemicMetadata().getCreated() != null) {
				metadata.append(FieldNames.CREATED, Date.from(metadatum.getSystemicMetadata().getCreated()));
			}
			if (metadatum.getSystemicMetadata().getModified() != null) {
				metadata.append(FieldNames.MODIFIED, Date.from(metadatum.getSystemicMetadata().getModified()));
			}
			if (metadatum.getSystemicMetadata().getStatus() != null) {
				metadata.append(FieldNames.STATUS, metadatum.getSystemicMetadata().getStatus().getStatusCode());
			}
		}
		GridFSUploadOptions options = new GridFSUploadOptions().metadata(metadata);

		ObjectId fileId;
		try {
			fileId = this.gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum storage failed. Element id: " + metadatum.getElementId(), e);
		}
		metadatum.setId(fileId.toString());
	}

	@Override
	public Metadatum update(Metadatum metadatum) throws MetadataStoreException {
		//Metadatum currentMetadatum;
		//if (metadatum.getId() != null) {
		//	currentMetadatum = get(metadatum.getId());
		//}
		Metadatum updatedMetadatum = null;
		if (metadatum.getValue() != null) {
			try {
				removeWhiteSpaceAndCalculateChecksum(metadatum);
			} catch (HashGenerationException e) {
				throw new MetadataStoreException(e);
			}

			// Update metadata of metadatum
			if (existsByIdAndChecksum(metadatum)) {
				updateMetadata(metadatum);
			// Insert new
			} else if (existsById(metadatum)) {
				String oldMetadatumId = metadatum.getId();
				insert(metadatum);
				updateStatus(oldMetadatumId, Status.INACTIVE);
				updatedMetadatum = metadatum;

				/*Metadatum oldMetadatum = new Metadatum();
				metadatum.setId(oldMetadatumId);
				delete(oldMetadatum);*/
			// No duplicate metadata
			} else if (existsByElementIdAndChecksum(metadatum)) {
				/*updatedMetadatum = */updateMetadata(metadatum);
				//updatedMetadatum = updatedMetadatum == null ? metadatum : updatedMetadatum;
			// Insert new
			} else {
				insert(metadatum);
				updatedMetadatum = metadatum;

			}
		// Update metadata of metadatum
		} else {
			/*updatedMetadatum = */updateMetadata(metadatum);
			//updatedMetadatum = updatedMetadatum == null ? metadatum : updatedMetadatum;
		}

		return updatedMetadatum;
	}

	@Override
	public void updateStatus(String id, Status status) throws MetadataStoreException {
		Map<String, Object> statusFieldAndValue = new HashMap<>();
		statusFieldAndValue.put(FieldNames.METADATA + "." + FieldNames.STATUS, status.getStatusCode());
		update(id, statusFieldAndValue);
	}

	private Metadatum update(String id, Map<String, Object> fieldsAndValues) throws MetadataStoreException {
		MetadataGridFSFile updated = null;

		if (id != null) {
			List<Bson> updates = fieldsAndValues.entrySet().stream().map(update -> Updates.set(update.getKey(), update.getValue())).collect(Collectors.toList());
			updates.add(Updates.currentDate(FieldNames.METADATA + "." + FieldNames.MODIFIED));

			updated = this.gridFsFilesCollection.findOneAndUpdate(
					Filters.eq(FieldNames.ID, new ObjectId(id)),
					Updates.combine(updates),
					new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
		}

		return updated != null ? updated.getMetadata() : null;
	}

	private Metadatum updateMetadata(Metadatum metadatum) throws MetadataStoreException {
		return update(metadatum.getId(), MetadataGridFS.metadatumToFieldsMap(metadatum));
	}

	private boolean existsById(Metadatum metadatum) throws MetadataStoreException {
		MetadataGridFSFile existing = null;
		if (metadatum.getId() != null) {
			existing = this.gridFsFilesCollection.find(
					Filters.eq(FieldNames.ID, new ObjectId(metadatum.getId()))).projection(Projections.include(FieldNames.ID)).limit(1).first();
			if (existing != null) {
				metadatum.setId(existing.getId());
			}
		}

		return existing != null;
	}

	private boolean existsByChecksum(Metadatum metadatum) {
		MetadataGridFSFile existing = null;

		if (metadatum.getChecksum() != null) {
			existing = this.gridFsFilesCollection.find(
					Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum()))
					.projection(Projections.include(FieldNames.ID)).limit(1).first();
			if (existing != null) {
				metadatum.setId(existing.getId());
			}
		}

		return existing != null;
	}

	private boolean existsByIdAndChecksum(Metadatum metadatum) {
		MetadataGridFSFile existing = null;

		if (metadatum.getId() != null && metadatum.getChecksum() != null) {
			existing = this.gridFsFilesCollection.find(Filters.and(
					Filters.eq(FieldNames.ID, new ObjectId(metadatum.getId())),
					Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum())
			)).projection(Projections.include(FieldNames.ID)).limit(1).first();

			/*if (existing != null) {
				metadatum.setId(existing.getId());
			}*/
		}

		return existing != null;
	}

	private boolean existsByElementIdAndChecksum(Metadatum metadatum) {
		MetadataGridFSFile existing = null;

		if (metadatum.getElementId() != null && metadatum.getChecksum() != null) {
			existing = this.gridFsFilesCollection.find(Filters.and(
					Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId())),
					Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum())
			)).projection(Projections.include(FieldNames.ID)).limit(1).first();

			if (existing != null) {
				metadatum.setId(existing.getId());
			}
		}

		return existing != null;
	}
	
	/*@Override
	public Metadatum get(Metadatum metadatum) throws MetadataStoreException {
		return download(metadatum.getId());
	}
*/
	@Override
	public Metadatum get(String id) throws MetadataStoreException {
		return get(id, false);
	}

	@Override
	public Metadatum get(String id, boolean lazy) throws MetadataStoreException {
		Metadatum metadatum = this.gridFsFilesCollection.find(Filters.eq(FieldNames.ID, new ObjectId(id))).limit(1).first().getMetadata();
		if (!lazy) {
			try {
				metadatum.setValue(download(metadatum.getId()).getValue());
			} catch (MetadataStoreException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return metadatum;
	}
	
	private Metadatum download(String id) throws MetadataStoreException {
		OutputStream metadatumStream = new ByteArrayOutputStream();
		try {
			this.gridFSBucket.downloadToStream(new ObjectId(id), metadatumStream);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum payload retrieval failed. id: [" + id + "]", e);
		}
		
		Metadatum metadatum = new Metadatum();
		metadatum.setId(id);
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
		return find(elementId, true, false);
	}
	
	@Override
	public List<Metadatum> find(String elementId, boolean lazy, boolean loadInactive) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		try {
			this.gridFsFilesCollection.find(
					loadInactive ?
					Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)) :
					Filters.and(
						Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)),
						Filters.eq(FieldNames.METADATA + "." + FieldNames.STATUS, Status.ACTIVE.getStatusCode())
					)
				).map(metadataGridFSFile -> {
					Metadatum metadatum = metadataGridFSFile.getMetadata();
					if (!lazy) {
						try {
							metadatum.setValue(download(metadataGridFSFile.getId()).getValue());
						} catch (MetadataStoreException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return metadatum;
				}).into(metadata);
		} catch (RuntimeException e) {
			throw new MetadataStoreException("Metadata retrieval failed. Element id: [" + elementId + "]" , e);
		}


		/*List<Metadatum> metadata = new ArrayList<>();
		try {
			this.gridFSBucket.get(Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId)))
					.map(gridFsFileToMetadatumTransformation(lazy)).into(metadata);
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}*/

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
	
	/*public boolean xPath(List<Metadatum> metadata, String xPath) throws XPathFactoryConfigurationException, MetadataStoreException {
		for (Metadatum metadatum: metadata) {
			try {
				if (new XPathEvaluator(XMLConverter.stringToNode(metadatum.getValue())).evaluate(xPath).size() > 0) {
					return true;
				}
			} catch (XPathEvaluationException | XMLConversionException e) {
				throw new MetadataStoreException("XPath failed", e);
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
			cursor = gridFSBucket.getQueryExecutor(
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

	
	/*public <T extends Element> T getQueryExecutor(T element, String xPath) throws MetadataStoreException {
		try {
			List<Metadatum> metadata = getQueryExecutor(element.getId());
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
			return getQueryExecutor(element.getId(), false).stream().filter(new Predicate<Metadatum>() {
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
	
	/*public <T extends Element> List<T> getQueryExecutor(List<T> elementIds, String xPath) throws MetadataStoreException {
		List<T> elements = null;
		try {
			elements = elementIds.stream().filter(new Predicate<T>() {
				@Override
				public boolean test(T element) {
					List<Metadatum> metadata = null;
					try {
						metadata = getQueryExecutor(element.getId(), false);
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
	
	/*public List<Metadatum> getQueryExecutor(Metadatum metadatum) {
		List<Metadatum> metadata = new ArrayList<>();
		MongoCursor<Metadatum> cursor = gridFSBucket
				.getQueryExecutor(buildMetadataFromDocument(metadatum)).map(new Function<GridFSFile, Metadatum>() {
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
	
	public List<Metadatum> getQueryExecutor(List<Metadatum> metadataList) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		for (Metadatum metadatum : metadataList) {
			getQueryExecutor(metadatum).stream().collect(Collectors.toCollection(() -> metadata));
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
	
	/*public boolean exists(Metadatum metadatum) {
		return this.gridFSBucket.get(buildMetadataFromDocument(metadatum)).filter(Projections.include("_id")).limit(1).first != null;
	}*/
	
	@Override
	public void delete(String metadatumId) {
		this.gridFSBucket.delete(new ObjectId(metadatumId));
	}
	
	@Override
	public void deleteAll(String elementId) throws MetadataStoreException {
		Map<String, Object> elementIdFilter = new HashMap<>();
		elementIdFilter.put(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(elementId));
		deleteAll(elementIdFilter);
	}

	@Override
	public void deleteAll(Map<String, Object> fieldsAndValues) throws MetadataStoreException {
		Bson filterQuery = Filters.and(fieldsAndValues.entrySet().stream()
				.map(fieldAndValue -> Filters.eq(fieldAndValue.getKey(), fieldAndValue.getValue())).toArray(Bson[]::new));

		try (MongoCursor<MetadataGridFSFile> cursor = this.gridFsFilesCollection.find(filterQuery).projection(Projections.include(FieldNames.ID)).iterator()) {
			while (cursor.hasNext()) {
				this.gridFSBucket.delete(new ObjectId(cursor.next().getId()));
			}
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum deletion from GridFS failed", e);
		}
	}

	private static String removeWhiteSpace(String metadata) {
		if (metadata != null && !metadata.isEmpty()) {
			try {
				return XMLFormatter.unindent(metadata).replaceAll(">\\s+<", "><").trim();
			} catch (TransformerException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return metadata;
	}

	private static void removeWhiteSpaceAndCalculateChecksum(Metadatum metadatum) throws HashGenerationException {
		if (metadatum.getContentType().contains("xml")) {
			metadatum.setValue(MetadataGridFS.removeWhiteSpace(metadatum.getValue()));
		} else {
			metadatum.setValue(metadatum.getValue());
		}

		//try {
		metadatum.setChecksum(HashGeneratorUtils.generateMD5(metadatum.getValue()));
		/*} catch (HashGenerationException e) {
			logger.error("Checksum generation failed");
		}*/
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

	private static Map<String, Object> metadatumToFieldsMap(Metadatum metadatum) {
		Map<String, Object> fieldsMap = new HashMap<>();
		if (metadatum.getElementId() != null) {
			fieldsMap.put(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()));
		}
		if (metadatum.getName() != null) {
			fieldsMap.put(FieldNames.METADATA + "." + FieldNames.NAME, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			fieldsMap.put(FieldNames.METADATA + "." + FieldNames.CONTENT_TYPE, metadatum.getContentType());
		}
		if (metadatum.getSystemicMetadata() != null) {
			if (metadatum.getSystemicMetadata().getModified() != null) {
				fieldsMap.put(FieldNames.METADATA + "." + FieldNames.MODIFIED, Date.from(metadatum.getSystemicMetadata().getModified()));
			}
			if (metadatum.getSystemicMetadata().getStatus() != null){
				fieldsMap.put(FieldNames.METADATA + "." + FieldNames.STATUS, metadatum.getSystemicMetadata().getStatus().getStatusCode());
			}
		}
		return fieldsMap;
	}

	private static Document metadatumToMetadataDocument(Metadatum metadatum) {
		Document metadataDocument = new Document();
		if (metadatum.getElementId() != null) {
			metadataDocument.append(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId()));
		}
		if (metadatum.getName() != null) {
			metadataDocument.append(FieldNames.METADATA + "." + FieldNames.NAME, new ObjectId(metadatum.getName()));
		}
		if (metadatum.getContentType() != null) {
			metadataDocument.append(FieldNames.METADATA + "." + FieldNames.CONTENT_TYPE, metadatum.getContentType());
		}
		if (metadatum.getSystemicMetadata() != null) {
			if (metadatum.getSystemicMetadata().getModified() != null) {
				metadataDocument.append(FieldNames.METADATA + "." + FieldNames.MODIFIED, Date.from(metadatum.getSystemicMetadata().getModified()));
			}
			if (metadatum.getSystemicMetadata().getStatus() != null){
				metadataDocument.append(FieldNames.METADATA + "." + FieldNames.STATUS, metadatum.getSystemicMetadata().getStatus().getStatusCode());
			}
		}
		return metadataDocument;
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

	public static void main(String[] args) {
		System.out.println(MetadataGridFS.removeWhiteSpace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<wcs:CoverageDescriptions xsi:schemaLocation=\"http://www.opengis.net/wcs/2.0 http://schemas.opengis" +
				".net/wcs/2.0/wcsAll.xsd\" xmlns:wcs=\"http://www.opengis.net/wcs/2.0\" xmlns:xsi=\"http://www" +
				".w3.org/2001/XMLSchema-instance\" xmlns:wcscrs=\"http://www.opengis" +
				".net/wcs/service-extension/crs/1.0\" xmlns:ows=\"http://www.opengis.net/ows/2.0\" " +
				"xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
				"  <wcs:CoverageDescription gml:id=\"frt00012bc6_07_if166l_trr3\" xmlns=\"http://www.opengis" +
				".net/gml/3.2\" xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\" xmlns:swe=\"http://www.opengis" +
				".net/swe/2.0\">\n" +
				"    <boundedBy>\n" +
				"      <Envelope srsName=\"http://localhost:8081/def/crs/PS/0/Mars-equirectangular\" axisLabels=\"E " +
				"N\" uomLabels=\"metre metre\" srsDimension=\"2\">\n" +
				"        <lowerCorner>7475301.7271 1342980.8343</lowerCorner>\n" +
				"        <upperCorner>7491472.7271 1357503.8343</upperCorner>\n" +
				"      </Envelope>\n" +
				"    </boundedBy>\n" +
				"    <wcs:CoverageId>frt00012bc6_07_if166l_trr3</wcs:CoverageId>\n" +
				"    <coverageFunction>\n" +
				"      <GridFunction>\n" +
				"        <sequenceRule axisOrder=\"+2 +1\">Linear</sequenceRule>\n" +
				"        <startPoint>0 0</startPoint>\n" +
				"      </GridFunction>\n" +
				"    </coverageFunction>\n" +
				"    <gmlcov:metadata>\n" +
				"      <gmlcov:Extension>\n" +
				"        <cat_start_time>2009-05-18T19:13:28.162</cat_start_time>\n" +
				"        <cat_solar_longitude>268.3650</cat_solar_longitude>\n" +
				"        <adding_target>MARS</adding_target>\n" +
				"      </gmlcov:Extension>\n" +
				"    </gmlcov:metadata>\n" +
				"    <domainSet>\n" +
				"      <RectifiedGrid dimension=\"2\" gml:id=\"frt00012bc6_07_if166l_trr3-grid\">\n" +
				"        <limits>\n" +
				"          <GridEnvelope>\n" +
				"            <low>0 0</low>\n" +
				"            <high>784 704</high>\n" +
				"          </GridEnvelope>\n" +
				"        </limits>\n" +
				"        <axisLabels>E N</axisLabels>\n" +
				"        <origin>\n" +
				"          <Point gml:id=\"frt00012bc6_07_if166l_trr3-origin\" " +
				"srsName=\"http://localhost:8081/def/crs/PS/0/Mars-equirectangular\">\n" +
				"            <pos>7475312.0271 1357493.5343</pos>\n" +
				"          </Point>\n" +
				"        </origin>\n" +
				"        <offsetVector srsName=\"http://localhost:8081/def/crs/PS/0/Mars-equirectangular\">20.6 " +
				"0</offsetVector>\n" +
				"        <offsetVector srsName=\"http://localhost:8081/def/crs/PS/0/Mars-equirectangular\">0 " +
				"-20.6</offsetVector>\n" +
				"      </RectifiedGrid>\n" +
				"    </domainSet>\n" +
				"    <gmlcov:rangeType>\n" +
				"      <swe:DataRecord>\n" +
				"        <swe:field name=\"band_1\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_2\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_3\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_4\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_5\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_6\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_7\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_8\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_9\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_10\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_11\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_12\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_13\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_14\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_15\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_16\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_17\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_18\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_19\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_20\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_21\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_22\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_23\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_24\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_25\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_26\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_27\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_28\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_29\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_30\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_31\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_32\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_33\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_34\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_35\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_36\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_37\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_38\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_39\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_40\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_41\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_42\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_43\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_44\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_45\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_46\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_47\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_48\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_49\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_50\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_51\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_52\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_53\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_54\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_55\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_56\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_57\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_58\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_59\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_60\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_61\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_62\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_63\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_64\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_65\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_66\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_67\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_68\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_69\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_70\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_71\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_72\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_73\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_74\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_75\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_76\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_77\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_78\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_79\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_80\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_81\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_82\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_83\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_84\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_85\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_86\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_87\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_88\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_89\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_90\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_91\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_92\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_93\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_94\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_95\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_96\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_97\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_98\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_99\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_100\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_101\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_102\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_103\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_104\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_105\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_106\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_107\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_108\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_109\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_110\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_111\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_112\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_113\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_114\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_115\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_116\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_117\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_118\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_119\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_120\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_121\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_122\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_123\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_124\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_125\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_126\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_127\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_128\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_129\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_130\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_131\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_132\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_133\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_134\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_135\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_136\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_137\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_138\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_139\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_140\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_141\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_142\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_143\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_144\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_145\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_146\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_147\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_148\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_149\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_150\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_151\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_152\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_153\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_154\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_155\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_156\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_157\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_158\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_159\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_160\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_161\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_162\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_163\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_164\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_165\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_166\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_167\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_168\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_169\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_170\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_171\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_172\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_173\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_174\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_175\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_176\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_177\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_178\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_179\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_180\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_181\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_182\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_183\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_184\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_185\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_186\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_187\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_188\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_189\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_190\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_191\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_192\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_193\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_194\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_195\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_196\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_197\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_198\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_199\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_200\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_201\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_202\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_203\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_204\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_205\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_206\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_207\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_208\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_209\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_210\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_211\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_212\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_213\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_214\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_215\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_216\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_217\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_218\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_219\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_220\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_221\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_222\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_223\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_224\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_225\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_226\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_227\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_228\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_229\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_230\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_231\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_232\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_233\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_234\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_235\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_236\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_237\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_238\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_239\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_240\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_241\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_242\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_243\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_244\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_245\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_246\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_247\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_248\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_249\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_250\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_251\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_252\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_253\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_254\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_255\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_256\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_257\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_258\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_259\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_260\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_261\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_262\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_263\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_264\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_265\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_266\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_267\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_268\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_269\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_270\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_271\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_272\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_273\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_274\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_275\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_276\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_277\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_278\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_279\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_280\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_281\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_282\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_283\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_284\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_285\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_286\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_287\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_288\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_289\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_290\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_291\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_292\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_293\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_294\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_295\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_296\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_297\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_298\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_299\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_300\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_301\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_302\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_303\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_304\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_305\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_306\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_307\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_308\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_309\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_310\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_311\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_312\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_313\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_314\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_315\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_316\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_317\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_318\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_319\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_320\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_321\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_322\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_323\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_324\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_325\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_326\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_327\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_328\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_329\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_330\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_331\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_332\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_333\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_334\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_335\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_336\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_337\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_338\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_339\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_340\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_341\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_342\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_343\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_344\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_345\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_346\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_347\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_348\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_349\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_350\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_351\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_352\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_353\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_354\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_355\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_356\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_357\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_358\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_359\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_360\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_361\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_362\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_363\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_364\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_365\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_366\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_367\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_368\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_369\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_370\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_371\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_372\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_373\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_374\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_375\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_376\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_377\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_378\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_379\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_380\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_381\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_382\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_383\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_384\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_385\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_386\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_387\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_388\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_389\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_390\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_391\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_392\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_393\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_394\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_395\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_396\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_397\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_398\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_399\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_400\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_401\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_402\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_403\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_404\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_405\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_406\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_407\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_408\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_409\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_410\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_411\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_412\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_413\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_414\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_415\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_416\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_417\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_418\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_419\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_420\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_421\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_422\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_423\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_424\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_425\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_426\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_427\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_428\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_429\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_430\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_431\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_432\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_433\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_434\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_435\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_436\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_437\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"        <swe:field name=\"band_438\">\n" +
				"          <swe:Quantity>\n" +
				"            <swe:uom code=\"10^0\"/>\n" +
				"          </swe:Quantity>\n" +
				"        </swe:field>\n" +
				"      </swe:DataRecord>\n" +
				"    </gmlcov:rangeType>\n" +
				"    <wcs:ServiceParameters>\n" +
				"      <wcs:CoverageSubtype>RectifiedGridCoverage</wcs:CoverageSubtype>\n" +
				"      <CoverageSubtypeParent xmlns=\"http://www.opengis.net/wcs/2.0\">\n" +
				"        <CoverageSubtype>AbstractDiscreteCoverage</CoverageSubtype>\n" +
				"        <CoverageSubtypeParent>\n" +
				"          <CoverageSubtype>AbstractCoverage</CoverageSubtype>\n" +
				"        </CoverageSubtypeParent>\n" +
				"      </CoverageSubtypeParent>\n" +
				"      <wcs:nativeFormat>application/octet-stream</wcs:nativeFormat>\n" +
				"    </wcs:ServiceParameters>\n" +
				"  </wcs:CoverageDescription>\n" +
				"</wcs:CoverageDescriptions>\n"));
	}
}
