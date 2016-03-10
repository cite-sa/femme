package gr.cite.exmms.datastore.mongo;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.DataElementMetadatum;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.datastore.exceptions.DatastoreException;

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
	
	@Test
	public void insert() {
		/*DataElement dataElement = createDemoDataElement();*/
		Collection collection = createDemoCollection();
		/*List<DataElement> dataElements = createDemoDataElements();*/
		try {
			/*mongo.insert(dataElement);*/
			mongo.insert(collection);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test
	public void find() {
		mongo.listDataElements();
	}*/
	
	public DataElement createDemoDataElement() {
		DataElement dataElement = new DataElement();
		dataElement.setName("testDataElement");
		dataElement.setEndpoint("http://www.cite-sa/gr/");
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new DataElementMetadatum("dc", "<dc><a>test value</a></dc>", "xml"));
		dataElement.setMetadata(metadata);
		
		DataElement embeddedDataElement = new DataElement();
		embeddedDataElement.setName("embeddedTestDataElement");
		embeddedDataElement.setEndpoint("http://www.cite-sa/gr/");
		
		dataElement.setDataElement(embeddedDataElement);
		
		return dataElement;
	}
	
	public List<DataElement> createDemoDataElements() {
		List<DataElement> dataElements = new ArrayList<>();
		for (int i = 0; i < 10; i ++) {
			DataElement dataElement = new DataElement();
			dataElement.setName("testDataElement" + i);
			dataElement.setEndpoint("http://www.cite-sa/gr/" + i);
			
			List<Metadatum> metadata = new ArrayList<>();
			metadata.add(new Metadatum("dc", "<dc><a>test value</a></dc>", "xml"));
			dataElement.setMetadata(metadata);
			
			dataElements.add(dataElement);
		}
		return dataElements;
	}
	
	public Collection createDemoCollection() {
		Collection collection = new Collection();
		collection.setName("testCollection");
		collection.setEndpoint("http://www.cite-sa/gr/");
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new Metadatum("dc", "<dc><a>test value</a></dc>", "xml"));
		collection.setMetadata(metadata);
		
		List<DataElement> dataElements = new ArrayList<>();
		dataElements.add(createDemoDataElement());
		collection.setDataElements(dataElements);
		
		return collection;
	}
}
