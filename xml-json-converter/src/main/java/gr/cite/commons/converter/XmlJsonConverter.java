package gr.cite.commons.converter;

import javax.xml.stream.XMLStreamException;

import gr.cite.commons.converter.json.JsonConverter;
import gr.cite.commons.converter.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.commons.converter.xml.XmlParser;

import java.io.IOException;

public class XmlJsonConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(XmlJsonConverter.class);
			
	public static String xmlToFemmeJson(String xml) throws XMLStreamException {
		XmlParser xmlParser = new XmlParser();
		return xmlParser.parseXml(xml).toString();
	}

	public static String femmeJsonToXml(String femmeJson) throws XMLStreamException, IOException {
		JsonParser jsonParser = new JsonParser();
		return jsonParser.parseJson(femmeJson);
	}
	
	public static String jsonToFemmeJson(String json) throws IOException {
		return JsonConverter.jsonToFemmeJson(json);
	}
	
	public static String femmeJsonToJson(String femmeJson) throws IOException {
		return JsonConverter.femmeJsonToJson(femmeJson);
	}
	
	public static void main(String[] args) throws IOException {
		String json = "{" +
						  "  \"id\": 14333," +
						  "  \"decimalLongitude\": -69.9445877075195," +
						  "  \"decimalLatitude\": 43.051689147949205," +
						  "  \"eventDate\": \"2005-07-11 01:42:36\"," +
						  "  \"institutionCode\": \"GoMA\"," +
						  "  \"collectionCode\": \"Platts Bank Aerial Survey\"," +
						  "  \"catalogNumber\": \"996.00\"," +
						  "  \"individualCount\": {\"count\":{\"recount\":1}}," +
						  "  \"datasetName\": \"Aerial survey of upper trophic level predators on PLatts Bank, Gulf of Maine\"," +
						  "  \"phylum\": \"Chordata\"," +
						  "  \"order\": \"Cetartiodactyla\"," +
						  "  \"family\": \"Balaenopteridae\"," +
						  "  \"genus\": \"Balaenoptera\"," +
						  "  \"scientificName\": \"Balaenoptera acutorostrata\"," +
						  "  \"originalScientificName\": \"Balaenoptera acutorostrata\"," +
						  "  \"scientificNameAuthorship\": \"Lacépède, 1804\"," +
						  "  \"obisID\": 739485," +
						  "  \"resourceID\": 267," +
						  "  \"yearcollected\": 2005," +
						  "  \"species\": \"Balaenoptera acutorostrata\"," +
						  "  \"qc\": 1073217151," +
						  "  \"aphiaID\": 137087," +
						  "  \"speciesID\": 739485," +
						  "  \"scientificNameID\": \"urn:lsid:marinespecies.org:taxname:137087\"," +
						  "  \"class\": \"Mammalia\"" +
						  "}";
		String femmeJson = XmlJsonConverter.jsonToFemmeJson(json);
		
		json = XmlJsonConverter.femmeJsonToJson(femmeJson);
		
		System.out.println(json);
		
		
	}
}
