package gr.cite.femme.engine.datastore.mongodb;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gr.cite.femme.core.exceptions.InvalidQueryOperationException;
import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.engine.query.construction.mongodb.CriterionMongo;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;

public class DatastoreMongoTest {
	private static final Logger logger = LoggerFactory.getLogger(DatastoreMongoTest.class);
	
	private MongoDatastore mongo;
	
	/*@Rule
	public FongoRule fongoRule = new FongoRule("femme-db");*/
	
	@Before
	public void init() {
		/*mongo = new MongoDatastore(fongoRule.getDatabase("femme-db"));*/
		//mongo = new MongoDatastore();
	}
	
	/*@After
	public void close() {
		mongo.close();
	}*/
	
	/*@Test
	public void queryDataElement() {
		try {
			List<DataElement> elements = getQueryExecutor.whereBuilder().expression(metadatum1).or().exists(metadatum2).and()
					.expression(getQueryExecutor.<DataElement>expressionFactory().expression(metadatum1).or().expression(metadatum2)).and()
					.isChildOf(dataElement2).and().isParentOf(dataElement1).execute().getQueryExecutor();
		} catch (UnsupportedQueryOperationException e1) {
			fail();
		}

	}*/
	
//	@Test
	public void testFind() throws DatastoreException, InvalidQueryOperationException, IOException {
		CriterionMongo finalCriterion = null;
		CriterionMongo dataElementCriterion = null;
		CriterionMongo collectionCriterion = null;
		
		dataElementCriterion = CriterionBuilderMongo.root().eq(FieldNames.NAME, "frt00014174_07_if166s_trr3").end();
		collectionCriterion = CriterionBuilderMongo.root()
				.inAnyCollection(Arrays.asList(CriterionBuilderMongo.root().eq(FieldNames.ENDPOINT, "http://access.planetserver.eu:8080/rasdaman/ows").end()))
				.end();
		
		/*criteria.where(FieldNames.NAME).eq("frt00009392_07_if166l_trr3")
			.inCollection(Criteria.getQueryExecutor().where(FieldNames.ENDPOINT)
					.eq("http://access.planetserver.eu:8080/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities"));*/
		/*criteria.where(FieldNames.ENDPOINT).eq("http://access.planetserver.eu:8080/rasdaman/ows?&SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCapabilities")
			.hasDataElements(Criteria.getQueryExecutor().where(FieldNames.NAME).eq("hrl0000c067_07_if185l_trr3"));*/
		/*criteria.orOperator(
				Criteria.getQueryExecutor().where("name").eq("testDataElement1"),
				Criteria.getQueryExecutor().where("name").eq("testDataElement2"));*/
		/*criteria.where(FieldNames.ENDPOINT).eq("http://access.planetserver.eu:8080/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities");*/
		/*finalCriterion = */
		QueryMongo query = QueryMongo.query().addCriterion(dataElementCriterion).addCriterion(collectionCriterion);
		
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(query);
		
		QueryMongo mongoQuery = mapper.readValue(json, QueryMongo.class);
		
		System.out.println(mongoQuery.build());
		
		/*List<DataElement> r = null;
		r = mongo.get(getQueryExecutor, DataElement.class, this.metadataStore).list();*//*.xPath("//a[text()=\"test value 1\"]");*/
		
		List<Duration> totalDuration = new ArrayList<>();
		/*or (int i = 0; i < 1; i ++) {
			Instant begin = Instant.now();
			
			r = mongo.<DataElement>getQueryExecutor(getQueryExecutor, DataElement.class).limit(1).list()
					.xPath("/wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:metadata/*[local-name()='adding_target'][text()=\"MARS\"]");
					.xPath("/*[local-name()='CoverageDescriptions']//*[local-name()='CoverageDescription']//*[local-name()='metadata']/*[local-name()='adding_target'][text()=\"MARS\"]");
			
			Instant end = Instant.now();
			
			totalDuration.add(Duration.between(begin, end));
		}*/
		/*Instant begin = Instant.now();
		
		r = mongo.<DataElement>getQueryExecutor(getQueryExecutor, DataElement.class).limit(1)
				.xPath("/wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:metadata/*[local-name()='adding_target'][text()=\"MARS\"]");
				.xPath("/*[local-name()='CoverageDescriptions']//*[local-name()='CoverageDescription']//*[local-name()='metadata']/*[local-name()='adding_target'][text()=\"MARS\"]");
		
		Instant end = Instant.now();
		
		System.out.println(Duration.between(begin, end));*/
		for (Duration duration: totalDuration) {
			System.out.println("Total time: " + duration);
		}
		
		/*System.out.println(r.size());*/
		
		/*System.out.println(r);*/
		
		/*try {
			mongo.delete(Criteria.getQueryExecutor().where(FieldNames.NAME).eq("testDataElement"), DataElement.class);
		} catch (DatastoreException | IllegalElementSubtype | InvalidCriteriaQueryOperation e) {
			e.printStackTrace();
		}*/
		
		/*DataElement updatedDataElement = createDemoDataElement(null, null);
		
		List<DataElement> des = new ArrayList<>();
		des.add(createDemoDataElement(null, null));
		des.add(createDemoDataElement(null, null));
		
		try {
			mongo.addToCollection(des, Criteria.getQueryExecutor().where(FieldNames.name()).eq("testCollection"));
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
		}*/
	}
	
	//@Test
	public void insertDataElement() {
		DataElement dataElement = createDemoDataElement(null, null);
		dataElement.setId(new ObjectId().toString());
		try {
			/*mongo.insert(dataElement);
			
			DataElement newDataElement = new DataElement();
			newDataElement.setId(dataElement.getId());
			newDataElement.setName("new name");
			
			mongo.insert(newDataElement);*/
			
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
		insertCollection();
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
	/*public void insertDataElements() {
		List<DataElement> dataElements = createDemoDataElements();
		try {
			mongo.insert(dataElements);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
		}
	}*/
	
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
		Metadatum metadatum;

		metadatum = new Metadatum();
		metadatum.setName("dc");
		metadatum.setValue("<dc><a>test value 1</a></dc>");
		metadatum.setContentType("xml");

		metadata.add(metadatum);

		metadatum = new Metadatum();
		metadatum.setName("test");
		metadatum.setValue("{testJson:{a:1, b:2}}");
		metadatum.setContentType("json");

		metadata.add(metadatum);

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
		Metadatum metadatum = new Metadatum();
		metadatum.setName("dc");
		metadatum.setValue("<dc><a>test value</a></dc>");
		metadatum.setContentType("xml");
		metadata.add(metadatum);
		collection.setMetadata(metadata);
		
		List<DataElement> dataElements = new ArrayList<>();
		dataElements.add(createDemoDataElement(null, null));
		dataElements.add(createDemoDataElement(null, null));
		collection.setDataElements(dataElements);
		
		return collection;
	}
}



/*db.collections.aggregate([
	{$match:
		{$or: [
		       		{"name": "testCollection5"},
		       		{"name": "testCollection7"}
		       ]
	   }
	},
	{$unwind: "$dataElements"},
	{$lookup : {
		from : "dataElements",
		localField : "dataElements._id",
		foreignField : "_id",
		as : "joinedDataElements"
	}},
	{$match : {
		"joinedDataElements": {
			$ne: []
		}
	}
}]).pretty()*/
