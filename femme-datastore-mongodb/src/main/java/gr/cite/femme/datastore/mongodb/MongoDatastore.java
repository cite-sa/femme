package gr.cite.femme.datastore.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.mongodb.bson.CollectionBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.DataElementBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.ElementBson;

public class MongoDatastore implements Datastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);
	private static final String METADATUM_ELEMENT_ID_PATH = "metadata.elementId";

	MongoDatastoreClient mongoClient;
	MongoCollection<Element> mongoCollection;

	public MongoDatastore() {
		mongoClient = new MongoDatastoreClient();
		mongoCollection = mongoClient.getElementsCollection();
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
			mongoCollection.insertMany(elementList);
		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting elements failed.");
		}
		return elementList;
	}

	@Override
	public DataElement update(Element element) throws DatastoreException {
		return null;
	}

	@Override
	public void remove(Element element) throws DatastoreException {
		Element deletedElement = null;
		if (element instanceof DataElement) {
			deletedElement = mongoCollection.findOneAndDelete(
				new DataElementBsonBuilder()
					.id(element.getId())
					.endpoint(element.getEndpoint())
					.name(element.getName())
					.systemicMetadata(element.getSystemicMetadata())
					.dataElement(((DataElement) element).getDataElement())
					.build()
			);
		} else if (element instanceof Collection) {
			deletedElement = mongoCollection.findOneAndDelete(
				new CollectionBsonBuilder()
					.id(element.getId())
					.endpoint(element.getEndpoint())
					.name(element.getName())
					.systemicMetadata(element.getSystemicMetadata())
					.dataElements(((Collection) element).getDataElements())
					.build()
			);
		}
		if (deletedElement != null) {
			MongoCursor<GridFSFile> cursor = mongoClient.getGridFSBucket()
					.find(Filters.eq(METADATUM_ELEMENT_ID_PATH, new ObjectId(deletedElement.getId()))).iterator();
			try {
				while (cursor.hasNext()) {
					mongoClient.getGridFSBucket().delete(cursor.next().getObjectId());
				}
			} catch(MongoGridFSException e) {
				logger.warn(e.getMessage(), e);
			} finally {
				cursor.close();
			}
		}
	}
	
	public void find(Element element) throws DatastoreException {
		ElementBson elementToFind = null;
		List<Element> elements = new ArrayList<>();
		if (element instanceof DataElement) {
			elementToFind = new DataElementBsonBuilder()
					.id(element.getId())
					.endpoint(element.getEndpoint())
					.name(element.getName())
					.systemicMetadata(element.getSystemicMetadata())
					.dataElement(((DataElement) element).getDataElement())
					.collections(((DataElement) element).getCollections())
					.build();
		} else if (element instanceof Collection) {
			elementToFind = new CollectionBsonBuilder()
				.id(element.getId())
				.endpoint(element.getEndpoint())
				.name(element.getName())
				.systemicMetadata(element.getSystemicMetadata())
				.dataElements(((Collection) element).getDataElements())
				.build();
		}
		MongoCursor<Element> cursor = mongoCollection.find(elementToFind).iterator();
		try {
			while (cursor.hasNext()) {
				elements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		System.out.println(elements);
	}
	
	@Override
	public void add(Element dataElement, Collection collection) throws DatastoreException {
	}

	@Override
	public void delete(Element dataElement, Collection collection) throws DatastoreException {
	}
	
	@Override
	public List<Element> listElements() {
		List<Element> elements = new ArrayList<>();
		MongoCursor<Element> cursor = mongoCollection.find().iterator();
		try {
			while (cursor.hasNext()) {
				elements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return elements;
	}

	@Override
	public List<DataElement> listDataElements() {
		List<DataElement> dataElements = new ArrayList<>();
		MongoCursor<Element> cursor = mongoCollection.find(Filters.exists("collections", true)).iterator();
		try {
			while (cursor.hasNext()) {
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
		MongoCursor<Element> cursor = mongoCollection.find(Filters.exists("collections", false)).iterator();
		try {
			while (cursor.hasNext()) {
				collections.add((Collection) cursor.next());
			}
		} finally {
			cursor.close();
		}
		return collections;
	}
}
