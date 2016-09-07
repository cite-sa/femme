package gr.cite.femme.query.test;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;

public class QueryTest {
	/*public static void main(String[] args) throws JsonProcessingException {
		CriterionClient criterion = CriterionBuilderClient.root()
				.or(Arrays.asList(CriterionBuilderClient.root().eq("id1", 5).end()))
				.and(Arrays.asList(CriterionBuilderClient.root().eq("id2", 5).end(), CriterionBuilderClient.root().eq("id3", 5).end()))
				.eq("id1", 5)
				.end();
		
		ObjectMapper mapper = new ObjectMapper();

		String json = mapper.writeValueAsString(criterion);
		System.out.println(json);
	}*/
	
	public static void main(String[] args) {
		DataElement dataElement = DataElement.builder().id("id").endpoint("endpoint").name("name").build();
		Collection collection = Collection.builder().id("id").endpoint("endpoint").name("name").build();
		System.out.println(dataElement);
	}
	
}
