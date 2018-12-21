package gr.cite.femme.engine.criteria.mongodb;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;

import gr.cite.femme.engine.query.construction.mongodb.CriterionMongo;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;

public class CriteriaQueryTest {
	
	/*private static final Logger logger = LoggerFactory.getLogger(CriteriaQueryTest.class);*/
	
	/*public void test() {
		CriteriaMongo criteria = CriteriaMongo.criteria();
		CriteriaMongo subCriteria1 = CriteriaMongo.criteria();
		CriteriaMongo subCriteria2 = CriteriaMongo.criteria();
		try {
			criteria.where(FieldNames.ENDPOINT).eq("http://www.cite-sa/gr/")
					.orOperator(
							subCriteria1.where("name").gt("5"),
							subCriteria2.where("endpoint").eq("sdf").and("name").eq("ddsf"));
			
			
		} catch (InvalidQueryOperationException e) {
			logger.error(e.getMessage(), e);
		}

		System.out.println(criteria.toString());

		Document mongoDoc = new Document(criteria.getCriteria());
		System.out.println(mongoDoc.toJson());
	}*/
	
	@Test
	public void testSerialization() throws IOException {
		/*ComparisonOperatorMongo comparisonOperator = new ComparisonOperatorMongo();
		comparisonOperator.eq("test", "testValue");
		
		System.out.println(comparisonOperator);*/
		
		/*LogicalOperatorMongo logicalOperator = new LogicalOperatorMongo();
		logicalOperator.or(Arrays.asList(CriterionBuilderMongo.root().eq("testField", "testValue").end()));*/
		
		ObjectMapper mapper = new ObjectMapper();
		
		CriterionMongo criterion = CriterionBuilderMongo
				.root().or(Arrays.asList(CriterionBuilderMongo.root().eq("testField1", "testValue1").end()))
				.lt("testField2", "testValue2").end();
		//System.out.println(criterion.toString());
		
		QueryMongo query = QueryMongo.query().addCriterion(criterion);
		
		System.out.println("String: " + query.build().get().toJson());
		
		
		String queryJson = mapper.writeValueAsString(query);
		System.out.println("json: " + queryJson);
		/*String json = mapper.writeValueAsString(criterion);
		System.out.println(json);*/
		
		QueryMongo queryParsed = mapper.readValue(queryJson, QueryMongo.class);
		String jsonParsed = mapper.writeValueAsString(queryParsed);
		System.out.println("Parsed: " + jsonParsed);
		/*ObjectMapper mapper = new ObjectMapper();

		String json = mapper.writeValueAsString(operator);*/
		/*System.out.println(comparisonOperator);*/
		/*System.out.println(criterion.toString());*/
	}
 
	  
	 /*@Test public void testQueryBuilders() throws
	 UnsupportedQueryOperationException { DataElement dataElement =
	 createDemoDataElement(null, null); Metadatum metadatumtest =
	 dataElement.getMetadata().get(0);
	 
	 Document getQueryExecutor = new RootQueryDocumentBuilder() .element(new
	 DataElementQueryDocumentBuilder(dataElement).hasMetadata( new
	 MetadataQueryDocumentBuilder()
	 .metadatum(metadatumtest).or().metadatum(metadatumtest) )).execute();
	 
	 MongoCursor<Element> cursor =
	 mongo.getElementCollection().getQueryExecutor(getQueryExecutor).iterator(); try { while
	 (cursor.hasNext()) { System.out.println(cursor.next()); } } finally {
	 cursor.close(); } System.out.println(getQueryExecutor); }
	 
	 private DataElement createDemoDataElement(String name, String endpoint) {
	 DataElement dataElement = new DataElement(); if (name != null) {
	 dataElement.setName(name); } else {
	 dataElement.setName("testDataElement"); } if (endpoint != null) {
	 dataElement.setEndpoint(endpoint); } else {
	 dataElement.setEndpoint("http://www.cite-sa/gr/"); }
	 
	 List<Metadatum> metadata = new ArrayList<>(); metadata.add(new
	 Metadatum("dc", "<dc><a>test value 1</a></dc>", "xml")); metadata.add(new
	 Metadatum("cidoc", "<dc><a>test value 2</a></dc>", "xml"));
	 dataElement.setMetadata(metadata);
	 
	 DataElement embeddedDataElement = new DataElement();
	 embeddedDataElement.setName("embeddedTestDataElement");
	 embeddedDataElement.setEndpoint("http://www.cite-sa/gr/");
	 
	 dataElement.setDataElement(embeddedDataElement);
	 
	 return dataElement; }*/
	 

	/*
	 * @Rule public FongoRule fongoRule = new FongoRule("femme-db");
	 */

	/*
	 * @Before public void init() { Fongo fongo = new Fongo("inmemory-mongodb");
	 * mongo = new MongoDatastore(fongoRule.getDatabase("femme-db")); }
	 */

	/*
	 * @After public void close() { mongo.close(); }
	 */
	/*
	 * @Test public void queryDataElement() {
	 * 
	 * DataElement dataElement1 = mock(DataElement.class); DataElement
	 * dataElement2 = mock(DataElement.class);
	 * 
	 * Metadatum metadatum1 = mock(Metadatum.class); Metadatum metadatum2 =
	 * mock(Metadatum.class);
	 * 
	 * CriteriaQuery<DataElement> getQueryExecutor = simpleMock();
	 * 
	 * try { List<DataElement> elements =
	 * getQueryExecutor.whereBuilder().expression(metadatum1).or().exists(metadatum2).and()
	 * .expression(getQueryExecutor.<DataElement>expressionFactory().expression(metadatum1)
	 * .or().expression(metadatum2)).and()
	 * .isChildOf(dataElement2).and().isParentOf(dataElement1).execute().getQueryExecutor(); }
	 * catch (UnsupportedQueryOperationException e1) { fail(); }
	 * 
	 * }
	 * 
	 * @Test public void queryMetadata() { DataElement e2 =
	 * mock(DataElement.class);
	 * 
	 * Metadatum m1 = mock(Metadatum.class); Metadatum m2 =
	 * mock(Metadatum.class);
	 * 
	 * CriteriaQuery<Metadatum> getQueryExecutor = simpleMock();
	 * 
	 * List<Metadatum> elements =
	 * getQueryExecutor.whereBuilder().expression(m1).or().exists(m2).and()
	 * .expression(getQueryExecutor.<Metadatum>expressionFactory().expression(m1).or().
	 * expression(m2)).and().isChildOf(e2).execute() .getQueryExecutor();
	 * 
	 * }
	 */
}
