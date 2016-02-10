package gr.cite.exmms.manager.criteria.serializer;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.exmms.manager.core.DataElement;
import gr.cite.exmms.manager.core.Metadatum;
import gr.cite.exmms.manager.criteria.CriteriaQuery;

public class CriteriaQuerySerializerTest {

	@Test
	public void test() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		

		CriteriaQuery<DataElement> query = new CriteriaQuerySerializer<>();
		

		Metadatum m1 = new Metadatum() {
			{
				setKey("id");
			}
		};
		Metadatum m2 = new Metadatum() {
			{
				setKey("localId");
			}
		};
		
		query.whereBuilder()//.exists(m1).and().exists(m2).or()
			.expression(
					
					query.expressionFactory()
					
						.expression(m1)
						.and()
						.expression(m2)
					
					).build();
		
		String json;
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(query);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}

}
