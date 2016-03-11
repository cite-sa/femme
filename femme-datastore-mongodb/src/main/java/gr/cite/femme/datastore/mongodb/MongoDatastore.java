package gr.cite.femme.datastore.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.exceptions.DatastoreException;

public class MongoDatastore implements Datastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	MongoDatastoreClient mongoClient;
	MongoCollection<Element> mongoCollection;
	/*GridFSBucket gridFSBucket;*/

	public MongoDatastore() {
		mongoClient = new MongoDatastoreClient();
		mongoCollection = mongoClient.getElementsCollection();
		/*gridFSBucket = mongoClient.getGridFSBucket();*/
	}

	public void close() {
		mongoClient.close();
	}
	
	@Override
	public <T extends Element> T insert(T element) throws DatastoreException {
		try {
			mongoCollection.insertOne(element);
		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting element failed.");
		}
		return element;
	}

	@Override
	public <T extends Element> List<T> insert(List<T> elementList) throws DatastoreException {
		try {
			/*for (T element: elementList) {
				insertMetadata(element.getMetadata());
			}*/
			/*mongoCollection.insertMany(elementList.stream().map(new Function<T, Document>() {
				@Override
				public Document apply(T element) {
					return MongoSerializer.createDocument(element);
				}
			}).collect(Collectors.toList()));*/
			mongoCollection.insertMany(elementList);
		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting elements failed.");
		}
		return elementList;
	}
	
	/*private void insertMetadata(List<Metadatum> metadata) {
		for (Metadatum metadatum : metadata) {
			String filename = UUID.randomUUID().toString();
			InputStream streamToUploadFrom = new ByteArrayInputStream(
					metadatum.getValue().getBytes(StandardCharsets.UTF_8));
			GridFSUploadOptions options = new GridFSUploadOptions().metadata(MongoSerializer.createDocument(metadatum));

			ObjectId fileId = gridFSBucket.uploadFromStream(filename, streamToUploadFrom, options);

			metadatum.setId(fileId.toString());
			metadatum.setFileName(filename);
		}
	}*/

	@Override
	public DataElement update(Element element) throws DatastoreException {
		return null;
	}

	@Override
	public void remove(Element element) throws DatastoreException {
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
		MongoCursor<Element> cursor = mongoCollection.find(Filters.exists("collection", false)).iterator();
		try {
			while (cursor.hasNext()) {
				/*dataElements.add((DataElement) MongoSerializer.createElement(cursor.next()));*/
				dataElements.add((DataElement) cursor.next());
			}
		} finally {
			cursor.close();
		}
		return dataElements;
	}

	@Override
	public List<Collection> listCollections() {
		List<Collection> collections = new ArrayList<>();
		MongoCursor<Element> cursor = mongoCollection.find(Filters.exists("collection", true)).iterator();
		try {
			while (cursor.hasNext()) {
				/*collections.add((Collection) MongoSerializer.createElement(cursor.next()));*/
				collections.add((Collection) cursor.next());
			}
		} finally {
			cursor.close();
		}
		return collections;
	}
}
