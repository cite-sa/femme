package gr.cite.exmms.datastore.mongo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.datastore.api.Datastore;
import gr.cite.exmms.datastore.exceptions.DatastoreException;
import gr.cite.exmms.datastore.mongo.serializer.MongoSerializer;

public class DatastoreMongo implements Datastore {
	private static final Logger logger = LoggerFactory.getLogger(DatastoreMongo.class);

	DatastoreMongoClient mongoClient;
	MongoCollection<Document> mongoCollection;
	GridFSBucket gridFSBucket;

	public DatastoreMongo() {
		mongoClient = new DatastoreMongoClient();
		mongoCollection = mongoClient.getCollection();
		gridFSBucket = mongoClient.getGridFSBucket();
	}

	public void close() {
		mongoClient.close();
	}

	@Override
	public <T extends Element> T insert(T element) throws DatastoreException {
		try {
			insertMetadata(element.getMetadata());
			mongoCollection.insertOne(MongoSerializer.createDocument(element));

		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting element failed.");
		}
		return element;
	}

	@Override
	public <T extends Element> List<T> insert(List<T> elementList) throws DatastoreException {
		try {
			for (T element: elementList) {
				insertMetadata(element.getMetadata());
			}
			mongoCollection.insertMany(elementList.stream().map(new Function<T, Document>() {
				@Override
				public Document apply(T element) {
					return MongoSerializer.createDocument(element);
				}
			}).collect(Collectors.toList()));
		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting elements failed.");
		}
		return elementList;
	}
	
	private void insertMetadata(List<Metadatum> metadata) {
		for (Metadatum metadatum : metadata) {
			String filename = UUID.randomUUID().toString();
			InputStream streamToUploadFrom = new ByteArrayInputStream(
					metadatum.getValue().getBytes(StandardCharsets.UTF_8));
			GridFSUploadOptions options = new GridFSUploadOptions().metadata(MongoSerializer.createDocument(metadatum));

			ObjectId fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);

			metadatum.setId(fileId.toString());
			metadatum.setFileName(filename);
		}
	}

	@Override
	public DataElement update(Element element) throws DatastoreException {
		return null;
	}

	@Override
	public void remove(Element element) throws DatastoreException {
	}
	
	@Override
	public DataElement find(DataElement dataElement) throws DatastoreException {
		return null;
	}

	@Override
	public DataElement find(Collection collection) throws DatastoreException {
		return null;
	}

	@Override
	public void add(Element dataElement, Collection collection) throws DatastoreException {
	}

	@Override
	public void delete(Element dataElement, Collection collection) throws DatastoreException {
	}

	@Override
	public List<DataElement> listDataElements() {
		List<DataElement> dataElements = new ArrayList<>();
		MongoCursor<Document> cursor = mongoCollection.find(Filters.exists("collection", false)).iterator();
		try {
			while (cursor.hasNext()) {
				dataElements.add((DataElement) MongoSerializer.createElement(cursor.next()));
			}
		} finally {
			cursor.close();
		}
		return dataElements;
	}

	@Override
	public List<Collection> listCollections() {
		List<Collection> collections = new ArrayList<>();
		MongoCursor<Document> cursor = mongoCollection.find(Filters.exists("collection", true)).iterator();
		try {
			while (cursor.hasNext()) {
				collections.add((Collection) MongoSerializer.createElement(cursor.next()));
			}
		} finally {
			cursor.close();
		}
		return collections;
	}
}
