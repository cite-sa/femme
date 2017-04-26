package gr.cite.commons.utils.xml;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Node;

import gr.cite.commons.utils.xml.exceptions.XMLConversionException;

public class XMLConverterTest {

	@Test
	public void testRemoveEmptyNodes() {
		String xml = "<a>"
				+ "<b/>"
				+ "<b>b</b>"
				+ "<c><b></b>c</c>"
				+ "</a>";
		
		Node node = null;
		try {
			node = XMLConverter.removeEmptyNodes(XMLConverter.stringToNode(xml, true));
		} catch (XMLConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String expected = "<a><b>b</b><c>c</c></a>";
		
		try {
			assertEquals(expected, XMLConverter.nodeToString(node));
		} catch (XMLConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
