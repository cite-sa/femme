package gr.cite.femme.criteria.serializer;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.criteria.serializer.CriteriaQuerySerializer;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;

public class CriteriaQuerySerializerTest {

	@Test
	public void allInOneComplexQueryTest() throws IOException, UnsupportedQueryOperationException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

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

		CriteriaQuery<DataElement> expectedQuery = new CriteriaQuerySerializer<>();
		expectedQuery.whereBuilder()/*.exists(m1).and().exists(m2).or()*/

				.expression(

						expectedQuery.<DataElement>expressionFactory().expression(m3).and().expression(m4))

				.or()

				.expression(

						expectedQuery.<DataElement>expressionFactory().expression(m3).and()

								.isChildOf(

										expectedQuery.<DataElement>expressionFactory().expression(m4))

								.or().isParentOf(m5))

				.and().isChildOf(c1).and().isParentOf(d1)

				.build();

		String serializedJson = null;
		try {
			serializedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(serializedJson);

		@SuppressWarnings("unchecked")
		CriteriaQuery<DataElement> actualQuery = mapper.readValue(serializedJson, CriteriaQuerySerializer.class);
		// System.out.println(query2);

		assertEquals(serializedJson, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualQuery));
	}

	@Test
	public void queryWithEmbededExpression() throws IOException, UnsupportedQueryOperationException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		Metadatum m1 = new Metadatum();
		m1.setName("m1");

		CriteriaQuery<DataElement> expectedQuery = new CriteriaQuerySerializer<>();
		/*expectedQuery.whereBuilder().expression(

				expectedQuery.<DataElement>expressionFactory().exists(m1)

		).build();*/

		String serializedJson = null;
		try {
			serializedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(serializedJson);

		@SuppressWarnings("unchecked")
		CriteriaQuery<DataElement> actualQuery = mapper.readValue(serializedJson, CriteriaQuerySerializer.class);
		// System.out.println(query2);

		assertEquals(serializedJson, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualQuery));
	}

}
