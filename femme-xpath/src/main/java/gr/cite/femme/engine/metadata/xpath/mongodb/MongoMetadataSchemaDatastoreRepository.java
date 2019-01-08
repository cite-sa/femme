package gr.cite.femme.engine.metadata.xpath.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.engine.metadata.xpath.mongodb.codecs.IndexableMetadatumCodecProvider;
import gr.cite.femme.engine.metadata.xpath.mongodb.codecs.MetadataSchemaCodecProvider;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoMetadataSchemaDatastoreRepository {

	private static final Logger logger = LoggerFactory.getLogger(MongoMetadataSchemaDatastoreRepository.class);

	private static final String DATABASE_HOST = "localhost";
	private static final int DATABASE_PORT = 27017;
	private static final String DATABASE_NAME = "metadata-schema-db-devel";
	private static final String TRANSFORMED_METADATA_COLLECTION_NAME = "metadata";
	private static final String METADATA_SCHEMAS_COLLECTION_NAME = "metadataSchemas";

	private MongoClient client;
	private MongoDatabase database;
	/*private MongoCollection<MaterializedPathsNode> materializedPaths;*/
	private MongoCollection<IndexableMetadatum> metadataCollection;
	private MongoCollection<MetadataSchema> collection;

	public MongoMetadataSchemaDatastoreRepository() {
		this(MongoMetadataSchemaDatastoreRepository.DATABASE_HOST, MongoMetadataSchemaDatastoreRepository.DATABASE_PORT, MongoMetadataSchemaDatastoreRepository.DATABASE_NAME);
	}

	public MongoMetadataSchemaDatastoreRepository(String host, int port, String name) {
		this(host, port,  name, false);
	}

	public MongoMetadataSchemaDatastoreRepository(String host, int port, String name, boolean metadataIndexStorage) {
		this.client = new MongoClient(host, port);
		this.database = this.client.getDatabase(name);

		List<CodecProvider> codecProviders = new ArrayList<>();
		codecProviders.add(new MetadataSchemaCodecProvider());
		if (metadataIndexStorage) {
			codecProviders.add(new IndexableMetadatumCodecProvider());
		}
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(codecProviders));

		/*materializedPaths = database.getCollection(MATERIALIZED_PATHS_COLLECTION_NAME, MaterializedPathsNode.class).withCodecRegistry(codecRegistry);*/
		if (metadataIndexStorage) {
			this.metadataCollection = this.database.getCollection(MongoMetadataSchemaDatastoreRepository.TRANSFORMED_METADATA_COLLECTION_NAME, IndexableMetadatum.class).withCodecRegistry(codecRegistry);
		}
		this.collection = this.database.getCollection(MongoMetadataSchemaDatastoreRepository.METADATA_SCHEMAS_COLLECTION_NAME, MetadataSchema.class).withCodecRegistry(codecRegistry);

		createIndexes();

	}

	@PreDestroy
	public void close() {
		logger.info("Closing connection to " + client.getAddress());
		this.client.close();
	}

	private void createIndexes() {
		IndexOptions uniqueIndex = new IndexOptions().unique(true);
		//IndexOptions notUniqueIndex = new IndexOptions().unique(false);

		if (this.metadataCollection != null) {
			this.metadataCollection.createIndex(Indexes.ascending("metadatumId"), uniqueIndex);
		}
		this.collection.createIndex(Indexes.ascending("checksum"), uniqueIndex);
		this.collection.createIndex(Indexes.ascending("schema"));
		this.collection.createIndex(Indexes.ascending("schema.array"));
		this.collection.createIndex(Indexes.ascending("schema.path"));
	}
	
	public String insert(MetadataSchema element) throws MetadataStoreException {
		try {
			this.collection.insertOne(element);
		} catch (MongoException e) {
			// Duplicate key error. Collection already exists
			if (11000 == e.getCode()) {
				throw new MetadataStoreException("Metadata schema [" + element.getChecksum() + "] already exists", e);
			} else {
				throw new MetadataStoreException("Metadata schema [" + element.getChecksum() + "] insertion failed", e);
			}
		}
		return element.getId();
	}
	
	public List<String> insert(List<MetadataSchema> elements) throws MetadataStoreException {
		if (elements == null || elements.size() == 0) throw new IllegalArgumentException("Elements must be contain elements");
		try {
			this.collection.insertMany(elements);
			return elements.stream().map(MetadataSchema::getId).collect(Collectors.toList());
		} catch (MongoException e) {
			throw new MetadataStoreException("Metadata schema bulk insertion failed.", e);
		}
	}
	
	public MetadataSchema update(MetadataSchema element) throws MetadataStoreException {
		try {
			return this.collection.findOneAndUpdate(
				Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
				new Document().append("$set", element),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema [" + element.getId() + "] update failed", e);
		}
	}
	
	public MetadataSchema replace(MetadataSchema element) throws MetadataStoreException {
		try {
			return this.collection.findOneAndReplace(
				Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
				element,
				new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
			);
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema [" + element.getId() + "] replace failed", e);
		}
	}
	
	public MetadataSchema delete(String id) throws MetadataStoreException {
		try {
			return this.collection.findOneAndDelete(Filters.eq(FieldNames.ID, new ObjectId(id)));
		} catch (IllegalArgumentException | MongoException e) {
			throw new MetadataStoreException("Metadata schema [" + id + "] deletion failed", e);
		}
	}
	
	public MetadataSchema getElementByProperty(String property, Object propertyValue) throws MetadataStoreException {
		try {
			return this.collection.find(Filters.eq(property, propertyValue)).limit(1).first();
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema [" + property + ":" + propertyValue + "] retrieval failed", e);
		}
	}
	
	public List<MetadataSchema> getElementsByProperty(String property, Object propertyValue) throws MetadataStoreException {
		try {
			return this.collection.find(Filters.eq(property, propertyValue)).into(new ArrayList<>());
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema list [" + property + ":" + propertyValue + "] retrieval failed", e);
		}
	}
	
	public MetadataSchema getElementByProperties(Map<String, String> propertiesAndValues) throws MetadataStoreException {
		try {
			Bson filter = Filters.and(propertiesAndValues.entrySet().stream().map(entry -> Filters.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
			return this.collection.find(filter).limit(1).first();
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema [" +
											 propertiesAndValues.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) +
											 "] retrieval failed", e);
		}
	}
	
	public List<MetadataSchema> getElementsByProperties(Map<String, String> propertiesAndValues) throws MetadataStoreException {
		try {
			Bson filter = Filters.and(propertiesAndValues.entrySet().stream().map(entry -> Filters.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
			return this.collection.find(filter).into(new ArrayList<>());
		} catch (Exception e) {
			throw new MetadataStoreException("Metadata schema [" +
											 propertiesAndValues.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) +
											 "] retrieval failed", e);
		}
	}
	
	public MetadataSchema get(Bson query) {
		return this.collection.find(query).limit(1).first();
	}
	
	public List<MetadataSchema> find(Bson query) {
		return this.collection.find(query).into(new ArrayList<>());
	}
	
	public FindIterable<MetadataSchema> lookup(Bson query) {
		return this.collection.find(query);
	}
	
	public AggregateIterable<MetadataSchema> aggregate(List<Bson> query) {
		return this.collection.aggregate(query);
	}
	
	public long count(Bson query) {
		return this.collection.count(query);
	}
	
	public MetadataSchema update(String id, Map<String, Object> fieldsAndValues) {
		List<Bson> updates = fieldsAndValues.entrySet().stream().map(fieldAndValue -> {
			Bson update;
			if (FieldNames.METADATA.equals(fieldAndValue.getKey())) {
				update = Updates.addToSet(fieldAndValue.getKey(), fieldAndValue.getValue());
			} else {
				update = Updates.set(fieldAndValue.getKey(), fieldAndValue.getValue());
			}
			return update;
		}).collect(Collectors.toList());
		
		updates.add(Updates.currentDate(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.MODIFIED));
		
		return this.collection.findOneAndUpdate(
			Filters.eq(FieldNames.ID, new ObjectId(id)),
			Updates.combine(updates),
			new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		);
	}
}
