package gr.cite.femme.engine.metadata.xpath.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import gr.cite.femme.core.datastores.DatastoreRepository;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.FieldNames;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MongoDatastoreRepository<T extends Element> implements DatastoreRepository<T> {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastoreRepository.class);

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<T> collection;
	
	protected MongoClient getClient() {
		return client;
	}
	
	protected void setClient(MongoClient client) {
		this.client = client;
	}
	
	protected MongoDatabase getDatabase() {
		return database;
	}
	
	protected void setDatabase(MongoDatabase database) {
		this.database = database;
	}
	
	protected MongoCollection<T> getCollection() {
		return collection;
	}
	
	protected void setCollection(MongoCollection<T> collection) {
		this.collection = collection;
	}
	
	public MongoDatastoreRepository(String[] hosts, String name, boolean sharding) {
		if (sharding) {
			this.client = new MongoClient(new MongoClientURI("mongodb://" + Arrays.stream(hosts).collect(Collectors.joining(","))));
		} else {
			this.client = new MongoClient(new MongoClientURI("mongodb://" + hosts[0]));
		}
		
		this.database = this.client.getDatabase(name);
	}
	
	protected abstract void createIndexes(MongoCollection<T> collection);
	
	@Override
	@PreDestroy
	public void close() {
		logger.info("Closing connection to " + this.client.getAddress());
		this.client.close();
	}
	
	@Override
	public String insert(T element) throws DatastoreException {
		try {
			this.collection.insertOne(element);
		} catch (MongoException e) {
			// Duplicate key error. Collection already exists
			if (11000 == e.getCode()) {
				throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " [" + element.getName() + "] already exists", e);
			} else {
				throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " [" + element.getName() + "] insertion failed", e);
			}
		}
		return element.getId();
	}
	
	@Override
	public List<String> insert(List<T> elements) throws DatastoreException {
		if (elements == null || elements.size() == 0) throw new IllegalArgumentException("Elements must be contain elements");
		try {
			this.collection.insertMany(elements);
			return elements.stream().map(T::getId).collect(Collectors.toList());
		} catch (MongoException e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " bulk insertion failed.", e);
		}
	}
	
	@Override
	public T update(T element) throws DatastoreException {
		try {
			return this.collection.findOneAndUpdate(
				Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
				new Document().append("$set", element),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " [" + element.getId() + "] update failed", e);
		}
	}
	
	@Override
	public T replace(T element) throws DatastoreException {
		try {
			return this.collection.findOneAndReplace(
				Filters.eq(FieldNames.ID, new ObjectId(element.getId())),
				element,
				new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
			);
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " [" + element.getId() + "] replace failed", e);
		}
	}
	
	@Override
	public T delete(String id) throws DatastoreException {
		try {
			return this.collection.findOneAndDelete(Filters.eq(FieldNames.ID, new ObjectId(id)));
		} catch (IllegalArgumentException | MongoException e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " [" + id + "] deletion failed", e);
		}
	}
	
	@Override
	public T getElementByProperty(String property, Object propertyValue) throws DatastoreException {
		try {
			return this.collection.find(Filters.eq(property, propertyValue)).limit(1).first();
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + "[" + property + ":" + propertyValue + "] retrieval failed", e);
		}
	}
	
	@Override
	public List<T> getElementsByProperty(String property, Object propertyValue) throws DatastoreException {
		try {
			return this.collection.find(Filters.eq(property, propertyValue)).into(new ArrayList<>());
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + " list [" + property + ":" + propertyValue + "] retrieval failed", e);
		}
	}
	
	@Override
	public T getElementByProperties(Map<String, String> propertiesAndValues) throws DatastoreException {
		try {
			Bson filter = Filters.and(propertiesAndValues.entrySet().stream().map(entry -> Filters.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
			return this.collection.find(filter).limit(1).first();
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + "[" +
						 propertiesAndValues.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) +
					"] retrieval failed", e);
		}
	}
	
	@Override
	public List<T> getElementsByProperties(Map<String, String> propertiesAndValues) throws DatastoreException {
		try {
			Bson filter = Filters.and(propertiesAndValues.entrySet().stream().map(entry -> Filters.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
			return this.collection.find(filter).into(new ArrayList<>());
		} catch (Exception e) {
			throw new DatastoreException(this.collection.getDocumentClass().getSimpleName() + "[" +
						 propertiesAndValues.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) +
					 "] retrieval failed", e);
		}
	}
	
	public T get(Bson query) {
		return this.collection.find(query).limit(1).first();
	}
	
	public List<T> find(Bson query) {
		return this.collection.find(query).into(new ArrayList<>());
	}
	
	public FindIterable<T> lookup(Bson query) {
		return this.collection.find(query);
	}
	
	public long count(Bson query) {
		return this.collection.count(query);
	}
	
	public T update(String id, Map<String, Object> fieldsAndValues) {
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
