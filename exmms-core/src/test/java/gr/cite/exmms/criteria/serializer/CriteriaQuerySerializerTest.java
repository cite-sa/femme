package gr.cite.exmms.criteria.serializer;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.serializer.CriteriaQuerySerializer;

public class CriteriaQuerySerializerTest {

	@Test
	public void test() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		CriteriaQuery<DataElement> expectedQuery = new CriteriaQuerySerializer<>();

		Metadatum m1 = new Metadatum();
		m1.setKey("m1");
		Metadatum m2 = new Metadatum();
		m2.setKey("m2");

		Metadatum m3 = new Metadatum();
		m3.setKey("m3");
		Metadatum m4 = new Metadatum();
		m4.setKey("m4");

		expectedQuery.whereBuilder().exists(m1).and().exists(m2).or().expression(

				expectedQuery.expressionFactory().expression(m3).and().expression(m4)

		).or().expression(

				expectedQuery.expressionFactory().expression(m3).and()
						.isChildOf(expectedQuery.expressionFactory().expression(m4))

		).build();

		String serializedJson = null;
		try {
			serializedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(serializedJson);

		@SuppressWarnings("unchecked")
		CriteriaQuery<DataElement> actualQuery = mapper.readValue(serializedJson, CriteriaQuerySerializer.class);
		// System.out.println(query2);

		assertEquals(serializedJson, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualQuery));
	}

}
