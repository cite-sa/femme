package gr.cite.femme.datastore.mongo;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.DataElementMetadatum;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.mongodb.MongoDatastore;

public class DatastoreMongoTest {
	private static final Logger logger = LoggerFactory.getLogger(DatastoreMongoTest.class);
	MongoDatastore mongo;
	
	@Before
	public void init() {
		mongo = new MongoDatastore();		
	}
	
	@After
	public void close() {
		mongo.close();
	}
	
	/*@Test*/
	public void insertDataElement() {
		DataElement dataElement = createDemoDataElement(null, null);
		try {
			mongo.insert(dataElement);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test*/
	public void insertCollection() {
		Collection collection = createDemoCollection();
		List<DataElement> dataElements = createDemoDataElements();
		try {
			mongo.insert(collection);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test*/
	public void insertDataElements() {
		List<DataElement> dataElements = createDemoDataElements();
		try {
			mongo.insert(dataElements);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Test
	public void removeDataElement() {
		DataElement dataElement = mongo.listDataElements().get(0); 
		try {
			mongo.remove(dataElement);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test*/
	public void findDataElement() {
		DataElement dataElement = mongo.listDataElements().get(0);
		try {
			mongo.remove(dataElement);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test*/
	public void listElements() {
		List<Element> elements = mongo.listElements();
		printElements(elements);
	}
	
	/*@Test*/
	public void listDataElements() {
		List<DataElement> dataElements = mongo.listDataElements();
		printDataElements(dataElements);
	}
	
	/*@Test*/
	public void listCollections() {
		List<Collection> collections = mongo.listCollections();
		printCollections(collections);
	}
	
	
	
	private DataElement createDemoDataElement(String name, String endpoint) {
		DataElement dataElement = new DataElement();
		if (name != null) {
			dataElement.setName(name);
		} else {
			dataElement.setName("testDataElement");
		}
		if (endpoint != null) {
			dataElement.setEndpoint(endpoint);
		} else {
			dataElement.setEndpoint("http://www.cite-sa/gr/");
		}
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new DataElementMetadatum("dc", "<dc><a>test value</a></dc>", "xml"));
		dataElement.setMetadata(metadata);
		
		DataElement embeddedDataElement = new DataElement();
		embeddedDataElement.setName("embeddedTestDataElement");
		embeddedDataElement.setEndpoint("http://www.cite-sa/gr/");
		
		dataElement.setDataElement(embeddedDataElement);
		
		return dataElement;
	}
	
	private List<DataElement> createDemoDataElements() {
		List<DataElement> dataElements = new ArrayList<>();
		for (int i = 0; i < 2; i ++) {
			dataElements.add(createDemoDataElement("testDataElement" + i, "http://www.cite-sa/gr/" + i));
		}
		return dataElements;
	}
	
	private Collection createDemoCollection() {
		Collection collection = new Collection();
		collection.setName("testCollection");
		collection.setEndpoint("http://www.cite-sa/gr/");
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new Metadatum("dc", "<dc><a>test value</a></dc>", "xml"));
		collection.setMetadata(metadata);
		
		List<DataElement> dataElements = new ArrayList<>();
		dataElements.add(createDemoDataElement(null, null));
		collection.setDataElements(dataElements);
		
		return collection;
	}
	private void printElements(List<Element> elements) {
		for (Element element : elements) {
			System.out.println(element.toString());
		}
	}
	private void printDataElements(List<DataElement> dataElements) {
		for (DataElement dataElement : dataElements) {
			System.out.println(dataElement.toString());
		}
	}
	private void printCollections(List<Collection> collections) {
		for (Collection collection : collections) {
			System.out.println(collection.toString());
		}
	}
}
