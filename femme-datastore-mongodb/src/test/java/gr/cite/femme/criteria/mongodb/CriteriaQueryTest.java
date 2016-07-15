package gr.cite.femme.criteria.mongodb;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.datastore.mongodb.DatastoreMongoTest;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.query.mongodb.Criteria;

public class CriteriaQueryTest {
	private static final Logger logger = LoggerFactory.getLogger(CriteriaQueryTest.class);

	@Test
	public void test() {
		Criteria criteria = new Criteria();
		Criteria subCriteria1 = new Criteria();
		Criteria subCriteria2 = new Criteria();
		try {
			criteria.where(FieldNames.ENDPOINT).eq("http://www.cite-sa/gr/")
					.orOperator(
							subCriteria1.where("name").gt("5"),
							subCriteria2.where("endpoint").eq("sdf").and("name").eq("ddsf"));
			
			
		} catch (InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
		}

		System.out.println(criteria.toString());

		Document mongoDoc = new Document(criteria.getCriteria());
		System.out.println(mongoDoc.toJson());
	}

	/*
	 * MongoDatastore mongo;
	 * 
	 * @Before public void init() { mongo = new
	 * MongoDatastore(fongoRule.getDatabase("femme-db")); mongo = new
	 * MongoDatastore(); }
	 * 
	 * @After public void close() { mongo.close(); }
	 * 
	 * @Test public void testQueryBuilders() throws
	 * UnsupportedQueryOperationException { DataElement dataElement =
	 * createDemoDataElement(null, null); Metadatum metadatumtest =
	 * dataElement.getMetadata().get(0);
	 * 
	 * Document query = new RootQueryDocumentBuilder() .element(new
	 * DataElementQueryDocumentBuilder(dataElement).hasMetadata( new
	 * MetadataQueryDocumentBuilder()
	 * .metadatum(metadatumtest).or().metadatum(metadatumtest) )).build();
	 * 
	 * MongoCursor<Element> cursor =
	 * mongo.getElementCollection().find(query).iterator(); try { while
	 * (cursor.hasNext()) { System.out.println(cursor.next()); } } finally {
	 * cursor.close(); } System.out.println(query); }
	 * 
	 * private DataElement createDemoDataElement(String name, String endpoint) {
	 * DataElement dataElement = new DataElement(); if (name != null) {
	 * dataElement.setName(name); } else {
	 * dataElement.setName("testDataElement"); } if (endpoint != null) {
	 * dataElement.setEndpoint(endpoint); } else {
	 * dataElement.setEndpoint("http://www.cite-sa/gr/"); }
	 * 
	 * List<Metadatum> metadata = new ArrayList<>(); metadata.add(new
	 * Metadatum("dc", "<dc><a>test value 1</a></dc>", "xml")); metadata.add(new
	 * Metadatum("cidoc", "<dc><a>test value 2</a></dc>", "xml"));
	 * dataElement.setMetadata(metadata);
	 * 
	 * DataElement embeddedDataElement = new DataElement();
	 * embeddedDataElement.setName("embeddedTestDataElement");
	 * embeddedDataElement.setEndpoint("http://www.cite-sa/gr/");
	 * 
	 * dataElement.setDataElement(embeddedDataElement);
	 * 
	 * return dataElement; }
	 */

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
	 * CriteriaQuery<DataElement> query = simpleMock();
	 * 
	 * try { List<DataElement> elements =
	 * query.whereBuilder().expression(metadatum1).or().exists(metadatum2).and()
	 * .expression(query.<DataElement>expressionFactory().expression(metadatum1)
	 * .or().expression(metadatum2)).and()
	 * .isChildOf(dataElement2).and().isParentOf(dataElement1).build().find(); }
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
	 * CriteriaQuery<Metadatum> query = simpleMock();
	 * 
	 * List<Metadatum> elements =
	 * query.whereBuilder().expression(m1).or().exists(m2).and()
	 * .expression(query.<Metadatum>expressionFactory().expression(m1).or().
	 * expression(m2)).and().isChildOf(e2).build() .find();
	 * 
	 * }
	 */
}
