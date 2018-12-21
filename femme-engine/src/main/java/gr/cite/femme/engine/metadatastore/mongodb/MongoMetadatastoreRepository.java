package gr.cite.femme.engine.metadatastore.mongodb;

import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.commons.utils.hash.HashGeneratorUtils;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XMLFormatter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.utils.Pair;
import gr.cite.femme.engine.metadatastore.mongodb.codecs.MetadataGridFSFileCodecProvider;
import gr.cite.femme.engine.metadatastore.mongodb.codecs.MetadataGridFSFileMetadataCodecProvider;
import gr.cite.femme.engine.datastore.mongodb.codecs.MetadatumJson;
import gr.cite.femme.engine.metadatastore.mongodb.codecs.MetadatumJsonCodecProvider;
import gr.cite.femme.core.model.FieldNames;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MongoMetadatastoreRepository implements MetadatastoreRepository {
	private static final Logger logger = LoggerFactory.getLogger(MongoMetadatastoreRepository.class);
	
	private static final String METADATA_COLLECTION_NAME = "metadataJson";
	private static final String METADATA_GRIDFS_BUCKET_NAME = "metadataGridFS";

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<MetadatumJson> metadataJson;
	
	private GridFSBucket gridFsBucket;
	private MongoCollection<MetadataGridFSFile> gridFsFilesCollection;
	
	private MetadataGridFSRepository metadataGridFSRepository;
	
	@Inject
	public MongoMetadatastoreRepository(String[] hosts, String name, boolean sharding) {
		this(hosts, name, MongoMetadatastoreRepository.METADATA_GRIDFS_BUCKET_NAME, sharding);
	}
	
	public MongoMetadatastoreRepository(String[] hosts, String name, String bucketName, boolean sharding) {
		if (sharding) {
			this.client = new MongoClient(new MongoClientURI("mongodb://" + Arrays.stream(hosts).collect(Collectors.joining(","))));
		} else {
			this.client = new MongoClient(new MongoClientURI("mongodb://" + hosts[0]));
		}
		
		this.database = this.client.getDatabase(name);
		
		CodecRegistry metadataJsonCodecRegistry = CodecRegistries
				.fromRegistries(CodecRegistries.fromProviders(new MetadatumJsonCodecProvider()), MongoClient.getDefaultCodecRegistry());
		this.metadataJson = this.database.getCollection(MongoMetadatastoreRepository.METADATA_COLLECTION_NAME, MetadatumJson.class).withCodecRegistry(metadataJsonCodecRegistry);

		this.gridFsBucket = GridFSBuckets.create(this.database, bucketName);

		CodecRegistry metadataGridFsFilesCodecRegistry = CodecRegistries
				.fromRegistries(
						CodecRegistries.fromProviders(new MetadataGridFSFileCodecProvider(), new MetadataGridFSFileMetadataCodecProvider()),
						MongoClient.getDefaultCodecRegistry());
		this.gridFsFilesCollection = this.database.getCollection(bucketName + ".files", MetadataGridFSFile.class).withCodecRegistry(metadataGridFsFilesCodecRegistry);

		createIndexes();
		
		this.metadataGridFSRepository = new MetadataGridFSRepository(this.gridFsBucket, this.gridFsFilesCollection);
	}
	
	@PreDestroy
	public void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}

	public MongoCollection<MetadatumJson> getMetadataJson() {
		return this.metadataJson;
	}
	
	GridFSBucket getMetadataGridFSBucket() {
		return this.gridFsBucket;
	}


	MongoCollection<MetadataGridFSFile> getMetadataGridFSFilesCollection() {
		return this.gridFsFilesCollection;
	}

	private void createIndexes() {
		IndexOptions uniqueIndexOptions = new IndexOptions();
		uniqueIndexOptions.unique(true);

		this.gridFsFilesCollection.createIndex(Indexes.ascending(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, FieldNames.METADATA + "." + FieldNames.CHECKSUM));
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
		
		upload(metadatum);
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
			fileId = this.gridFsBucket.uploadFromStream(filename, streamToUploadFrom, options);
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum storage failed. Element id: " + metadatum.getElementId(), e);
		}
		metadatum.setId(fileId.toString());
	}
	
	@Override
	public Metadatum update(Metadatum metadatum) throws MetadataStoreException {
		Metadatum newMetadatum = null;
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
				newMetadatum = metadatum;
				
				updateStatus(oldMetadatumId, Status.INACTIVE);
				
				// No duplicate metadata
			} else if (existsByElementIdAndChecksum(metadatum)) {
				updateMetadata(metadatum);
				// Insert new
			} else {
				insert(metadatum);
				newMetadatum = metadatum;
				
			}
			// Update metadata of metadatum
		} else {
			updateMetadata(metadatum);
			//updatedMetadatum = updatedMetadatum == null ? metadatum : updatedMetadatum;
		}
		
		return newMetadatum;
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
				Filters.eq(FieldNames.ID, new ObjectId(id)), Updates.combine(updates),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
			);
		}
		
		return updated != null ? updated.getMetadata() : null;
	}
	
	private Metadatum updateMetadata(Metadatum metadatum) throws MetadataStoreException {
		return update(metadatum.getId(), metadatumToFieldsMap(metadatum));
	}
	
	private boolean existsById(Metadatum metadatum) throws MetadataStoreException {
		return metadatum.getId() != null && existsByFilter(Filters.eq(FieldNames.ID, new ObjectId(metadatum.getId())));
	}
	
	private boolean existsByChecksum(Metadatum metadatum) {
		if (metadatum.getChecksum() == null) return false;
		
		String existingMetadatumId = findByFilter(Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum()));
		if (existingMetadatumId != null) {
			metadatum.setId(existingMetadatumId);
		}
		
		return existingMetadatumId != null;
	}
	
	private boolean existsByIdAndChecksum(Metadatum metadatum) {
		return metadatum.getId() != null && metadatum.getChecksum() != null
				   && existsByFilter(Filters.and(
			Filters.eq(FieldNames.ID, new ObjectId(metadatum.getId())),
			Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum())
		));
	}
	
	private boolean existsByElementIdAndChecksum(Metadatum metadatum) {
		if (metadatum.getElementId() == null) return false;
		if (metadatum.getChecksum() == null) return false;
		
		String existingMetadatumId = findByFilter(Filters.and(
			Filters.eq(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, new ObjectId(metadatum.getElementId())),
			Filters.eq(FieldNames.METADATA + "." + FieldNames.CHECKSUM, metadatum.getChecksum())
		));
		if (existingMetadatumId != null) {
			metadatum.setId(existingMetadatumId);
		}
		return existingMetadatumId != null;
	}
	
	private boolean existsByFilter(Bson filter) {
		return this.gridFsFilesCollection.find(filter).projection(Projections.include(FieldNames.ID)).limit(1).first() != null;
	}
	
	private String findByFilter(Bson filter) {
		MetadataGridFSFile file = this.gridFsFilesCollection.find(filter).projection(Projections.include(FieldNames.ID)).limit(1).first();
		return file != null ? file.getId() : null;
		
	}
	
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
			this.gridFsBucket.downloadToStream(new ObjectId(id), metadatumStream);
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
			cursor = this.gridFsBucket.find().map(gridFsFileToMetadatumTransformation(lazy)).iterator();
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return cursor;
	}
	
	@Override
	public MongoCursor<Metadatum> findAllBeforeTimestamp(Instant timestamp) throws MetadataStoreException {
		MongoCursor<Metadatum> cursor;
		try {
			cursor = this.gridFsBucket.find(Filters.lte(FieldNames.METADATA + "." + FieldNames.MODIFIED, Date.from(timestamp)))
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
	public List<Metadatum> find(List<String> elementIds) throws MetadataStoreException {
		return find(elementIds, true);
	}
	
	@Override
	public List<Metadatum> find(List<String> elementIds, boolean lazy) throws MetadataStoreException {
		return find(elementIds, lazy, false);
	}
	
	@Override
	public List<Metadatum> find(List<String> elementIds, boolean lazy, boolean loadInactive) throws MetadataStoreException {
		List<Metadatum> metadata = new ArrayList<>();
		try {
			List<ObjectId> elementObjectIds = elementIds.stream().map(ObjectId::new).collect(Collectors.toList());
			
			Bson metadatumByElementIdFilter = Filters.in(FieldNames.METADATA + "." + FieldNames.METADATA_ELEMENT_ID, elementObjectIds);
			this.gridFsFilesCollection.find(
				loadInactive ? metadatumByElementIdFilter :
					Filters.and(metadatumByElementIdFilter, Filters.eq(FieldNames.METADATA + "." + FieldNames.STATUS, Status.ACTIVE.getStatusCode()))
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
			throw new MetadataStoreException("Metadata retrieval failed. Element ids: [" + elementIds + "]" , e);
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
	
	public Pair<ObjectId, String> getMetadatumInfo(Metadatum metadatum) throws MetadataStoreException {
		Pair<ObjectId, String> metadatumInfo = null;
		MongoCursor<GridFSFile> cursor = this.gridFsBucket.find(buildMetadataFromDocument(metadatum))
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
		this.gridFsBucket.delete(new ObjectId(metadatumId));
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
				this.gridFsBucket.delete(new ObjectId(cursor.next().getId()));
			}
		} catch (MongoGridFSException e) {
			throw new MetadataStoreException("Metadatum deletion from GridFS failed", e);
		}
	}
	
	private void removeWhiteSpaceAndCalculateChecksum(Metadatum metadatum) throws HashGenerationException {
		if (metadatum.getContentType().contains("xml")) {
			metadatum.setValue(removeWhiteSpace(metadatum.getValue()));
		} else {
			metadatum.setValue(metadatum.getValue());
		}
		
		metadatum.setChecksum(HashGeneratorUtils.generateMD5(metadatum.getValue()));
	}
	
	private String removeWhiteSpace(String metadata) {
		if (metadata != null && !metadata.isEmpty()) {
			try {
				return XMLFormatter.unindent(metadata).replaceAll(">\\s+<", "><").trim();
			} catch (TransformerException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return metadata;
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
	
	private Map<String, Object> metadatumToFieldsMap(Metadatum metadatum) {
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
}
