package gr.cite.femme.datastore.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fakemongo.Fongo;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.IllegalElementSubtype;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.bson.DataElementBson;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Query;
import gr.cite.femme.query.mongodb.QueryOptions;

public class DatastoreMongoTest {
	private static final Logger logger = LoggerFactory.getLogger(DatastoreMongoTest.class);
	MongoDatastore mongo;
	
	/*@Rule
	public FongoRule fongoRule = new FongoRule("femme-db");*/
	
	@Before
	public void init() {
		/*mongo = new MongoDatastore(fongoRule.getDatabase("femme-db"));*/
		mongo = new MongoDatastore();
	}
	
	@After
	public void close() {
		mongo.close();
	}
	
	/*@Test
	public void queryDataElement() {
		try {
			List<DataElement> elements = query.whereBuilder().expression(metadatum1).or().exists(metadatum2).and()
					.expression(query.<DataElement>expressionFactory().expression(metadatum1).or().expression(metadatum2)).and()
					.isChildOf(dataElement2).and().isParentOf(dataElement1).build().find();
		} catch (UnsupportedQueryOperationException e1) {
			fail();
		}

	}*/
	
	@Test
	public void testFind() {
		Criteria criteria = new Criteria();
		
		try {
			criteria.where(FieldNames.NAME).eq("testDataElement9")
			/*.hasDataElements(Criteria.query().where(FieldNames.NAME).eq("testDataElement8"))*/;
			/*criteria.orOperator(
					Criteria.query().where("name").eq("testDataElement1"),
					Criteria.query().where("name").eq("testDataElement2"));*/
		} catch (InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
		}
		Query query = new Query();
		query.addCriteria(criteria);
		
		List<DataElement> r = null;
		try {
			/*r = mongo.<DataElement>find(query, DataElement.class).xPath("//a[text()=\"test value 1\"]");*/
			r = mongo.<DataElement>find(query, DataElement.class).xPath("//a[text()=\"test value 1\"]");
		} catch (IllegalElementSubtype e) {
			e.printStackTrace();
		}
		
		System.out.println(r);
		
		/*try {
			mongo.delete(Criteria.query().where(FieldNames.NAME).eq("testDataElement"), DataElement.class);
		} catch (DatastoreException | IllegalElementSubtype | InvalidCriteriaQueryOperation e) {
			e.printStackTrace();
		}*/
		
		/*DataElement updatedDataElement = createDemoDataElement(null, null);
		
		List<DataElement> des = new ArrayList<>();
		des.add(createDemoDataElement(null, null));
		des.add(createDemoDataElement(null, null));
		
		try {
			mongo.addToCollection(des, Criteria.query().where(FieldNames.name()).eq("testCollection"));
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
		}*/
	}
	
	/*@Test*/
	public void insertDataElement() {
		DataElement dataElement = createDemoDataElement(null, null);
		try {
			mongo.insert(dataElement);
			System.out.println("ok");
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/*@Test*/
	public void insertCollections() {
		insertCollection();
		insertCollection();
	}
	
	/*@Test*/
	public void insertCollection() {
		Collection collection = createDemoCollection();
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
	
	/*@Test*/
	/*public void removeDataElement() {
		DataElement dataElement = null;
		try {
			dataElement = mongo.listDataElements().get(0);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		} 
		try {
			mongo.remove(dataElement);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}*/
	
	private DataElement createDemoDataElement(String name, String endpoint) {
		DataElement dataElement = new DataElement();
		if (name != null) {
			dataElement.setName(name);
		} else {
			dataElement.setName("testDataElement" + RandomUtils.nextInt(0, 10));
		}
		if (endpoint != null) {
			dataElement.setEndpoint(endpoint);
		} else {
			dataElement.setEndpoint("http://www.cite-sa/gr/" + RandomUtils.nextInt(0, 10));
		}
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new Metadatum("dc", "<dc><a>test value 1</a></dc>", "xml"));
		metadata.add(new Metadatum("cidoc", "<dc><a>test value 2</a></dc>", "xml"));
		dataElement.setMetadata(metadata);
		
		List<DataElement> embeddedDataElements = new ArrayList<>();
		DataElement embeddedDataElement1 = new DataElement();
		embeddedDataElement1.setName("embeddedTestDataElement" +  + RandomUtils.nextInt(0, 10));
		embeddedDataElement1.setEndpoint("http://www.cite-sa/gr/" +  + RandomUtils.nextInt(0, 10));
		embeddedDataElements.add(embeddedDataElement1);
		
		DataElement embeddedDataElement2 = new DataElement();
		embeddedDataElement2.setName("embeddedTestDataElement" +  + RandomUtils.nextInt(11, 20));
		embeddedDataElement2.setEndpoint("http://www.cite-sa/gr/" +  + RandomUtils.nextInt(11, 20));
		embeddedDataElements.add(embeddedDataElement2);
		
		dataElement.setDataElements(embeddedDataElements);
		
		return dataElement;
	}
	
	private List<DataElement> createDemoDataElements() {
		List<DataElement> dataElements = new ArrayList<>();
		for (int i = 0; i < 5; i ++) {
			dataElements.add(createDemoDataElement("testDataElement" + i, "http://www.cite-sa/gr/" + i));
		}
		return dataElements;
	}
	
	private Collection createDemoCollection() {
		Collection collection = new Collection();
		collection.setName("testCollection" + RandomUtils.nextInt(0, 10));
		collection.setEndpoint("http://www.cite-sa/gr/");
		
		List<Metadatum> metadata = new ArrayList<>();
		metadata.add(new Metadatum("dc", "<dc><a>test value</a></dc>", "xml"));
		collection.setMetadata(metadata);
		
		List<DataElement> dataElements = new ArrayList<>();
		dataElements.add(createDemoDataElement(null, null));
		dataElements.add(createDemoDataElement(null, null));
		collection.setDataElements(dataElements);
		
		return collection;
	}
}
