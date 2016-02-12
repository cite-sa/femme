package gr.cite.exmms.criteria.walker;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.serializer.CriteriaQuerySerializer;
import gr.cite.exmms.criteria.utils.PrintCriteriaQuery;

public class CriteriaWalkerExamplesTest {

	private CriteriaQuerySerializer<DataElement> expectedQuery;

	@Before
	public void init() throws UnsupportedQueryOperationException {
		Metadatum m1 = new Metadatum();
		m1.setName("m1");
		Metadatum m2 = new Metadatum();
		m2.setName("m2");

		Metadatum m3 = new Metadatum();
		m3.setName("m3");
		Metadatum m4 = new Metadatum();
		m4.setName("m4");

		Metadatum m5 = new Metadatum();
		m5.setName("m5");

		DataElement d1 = new DataElement();
		d1.setId("d1");

		Collection c1 = new Collection();
		c1.setId("c1");

		this.expectedQuery = new CriteriaQuerySerializer<>();
		expectedQuery.whereBuilder().exists(m1).and().exists(m2).or()

				.expression(

						expectedQuery.<DataElement> expressionFactory().expression(m3).and().expression(m4))

				.or()

				.expression(

						expectedQuery.<DataElement> expressionFactory().expression(m3).and()

								.isChildOf(

										expectedQuery.<DataElement> expressionFactory().expression(m4))

								.or().isParentOf(m5))

				.and().isChildOf(c1).and().isParentOf(d1)

				.build();
	}

	@Test
	public void test1() throws UnsupportedQueryOperationException {

		CriteriaQuery<DataElement> datastoreQuery = new PrintCriteriaQuery<>();

		new CriteriaWalker<DataElement>(expectedQuery, datastoreQuery).walk();

		assertEquals(
				"exists m1 and exists m2 or ( m3 = null and m4 = null) or ( m3 = null and isChildOf( m4 = null) or isParentOf m5) and isChildOf c1 and isParentOf d1",
				datastoreQuery.toString());

	}

	@Test
	public void test2() throws UnsupportedQueryOperationException {

		CriteriaQuery<DataElement> datastoreQuery = new CriteriaWalker<DataElement>(expectedQuery,
				new PrintCriteriaQuery<>()).walk();

		assertEquals(
				"exists m1 and exists m2 or ( m3 = null and m4 = null) or ( m3 = null and isChildOf( m4 = null) or isParentOf m5) and isChildOf c1 and isParentOf d1",
				datastoreQuery.toString());

	}

}
