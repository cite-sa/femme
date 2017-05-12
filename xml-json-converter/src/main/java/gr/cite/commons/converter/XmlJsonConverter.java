package gr.cite.commons.converter;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.commons.converter.json.JsonDocument;
import gr.cite.commons.converter.xml.XmlParser;

public class XmlJsonConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(XmlJsonConverter.class);
			
	public static String xmlToJson(String xml) throws XMLStreamException {
		XmlParser xmlParser = new XmlParser();
		return xmlParser.parseXml(xml).toString();
		
	}

}
