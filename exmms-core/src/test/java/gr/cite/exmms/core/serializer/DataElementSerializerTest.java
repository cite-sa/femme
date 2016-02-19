package gr.cite.exmms.core.serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.DataElementMetadatum;

public class DataElementSerializerTest {

	@Test
	public void serializeDataElement() {
		ObjectMapper mapper = new ObjectMapper();

		DataElement dataElement = new DataElement();
		dataElement.setId(UUID.randomUUID().toString());
		dataElement.setEndpoint(UUID.randomUUID().toString());
		dataElement.setMetadata(
				Arrays.asList(new DataElementMetadatum(UUID.randomUUID().toString(), "test", "testValue", "xml")));
		
		try {
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataElement);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		 
	}

}
