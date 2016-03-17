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
import gr.cite.femme.datastore.mongodb.bson.CollectionBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.DataElementBsonBuilder;
import gr.cite.femme.datastore.mongodb.bson.ElementBson;

public class MongoDatastore implements Datastore {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	MongoDatastoreClient mongoClient;
	MongoCollection<Element> elementCollection;
	/*MongoCollection<Metadatum> metadataCollection;*/

	public MongoDatastore() {
		mongoClient = new MongoDatastoreClient();
		elementCollection = mongoClient.getElementCollection();
		/*metadataCollection = mongoClient.getMetadataCollection();*/
	}

	public void close() {
		mongoClient.close();
	}
	
	@Override
	public <T extends Element> T insert(T element) throws DatastoreException {
		try {
			elementCollection.insertOne(element);
		} catch (MongoException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException("Inserting element failed.");
		}
		return element;
	}

	@Override
	public <T extends Element> List<T> insert(List<T> elementList) throws DatastoreException {
		try {
			elementCollection.insertMany(elementList);
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
		deletedElement = elementCollection.findOneAndDelete(buildElementBson(element));
		if (deletedElement != null) {
			mongoClient.getMetadatumGridFS().delete(deletedElement.getId());
		}
	}
	
	public void find(Element element) throws DatastoreException {
		List<Element> elements = new ArrayList<>();
		MongoCursor<Element> cursor = elementCollection.find(buildElementBson(element)).iterator();
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
		MongoCursor<Element> cursor = elementCollection.find().iterator();
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
		MongoCursor<Element> cursor = elementCollection.find(Filters.exists("collections", true)).iterator();
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
		MongoCursor<Element> cursor = elementCollection.find(Filters.exists("collections", false)).iterator();
		try {
			while (cursor.hasNext()) {
				collections.add((Collection) cursor.next());
			}
		} finally {
			cursor.close();
		}
		return collections;
	}
	
	private ElementBson buildElementBson(Element element) {
		ElementBson elementBson = null;
		if (element instanceof DataElement) {
			elementBson = new DataElementBsonBuilder()
				.id(element.getId())
				.endpoint(element.getEndpoint())
				.name(element.getName())
				.systemicMetadata(element.getSystemicMetadata())
				.dataElement(((DataElement) element).getDataElement())
				.build();
		} else if (element instanceof Collection) {
			elementBson = new CollectionBsonBuilder()
				.id(element.getId())
				.endpoint(element.getEndpoint())
				.name(element.getName())
				.systemicMetadata(element.getSystemicMetadata())
				.dataElements(((Collection) element).getDataElements())
				.build();
		}
		return elementBson;
	}
}
