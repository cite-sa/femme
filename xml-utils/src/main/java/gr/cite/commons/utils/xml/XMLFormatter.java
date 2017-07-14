package gr.cite.commons.utils.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XMLFormatter {
	private static final String INDENT_AMOUNT = "2";

	public static String indent(String unformattedXml) throws TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = new net.sf.saxon.TransformerFactoryImpl().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", XMLFormatter.INDENT_AMOUNT);

		Source xmlInput = new StreamSource(new StringReader(unformattedXml));
		StreamResult xmlOutput = new StreamResult(new StringWriter());

			transformer.transform(xmlInput, xmlOutput);

		return xmlOutput.getWriter().toString();
	}

	public static String unindent(String unformattedXml) throws TransformerFactoryConfigurationError, TransformerException {
		Source xmlInput = new StreamSource(new StringReader(unformattedXml));
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
		//transformerFactory.setAttribute("indent-number", XMLFormatter.INDENT_AMOUNT);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.transform(xmlInput, xmlOutput);

		return xmlOutput.getWriter().toString();
	}

	public static String format(String unformattedXml) throws TransformerFactoryConfigurationError, TransformerException {
		Source xmlInput = new StreamSource(new StringReader(unformattedXml));
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
		//transformerFactory.setAttribute("indent-number", XMLFormatter.INDENT_AMOUNT);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(xmlInput, xmlOutput);

		String xml = xmlOutput.getWriter().toString();

		return format2(xml);
	}

	private static Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String format2(String unformattedXml)
			throws TransformerException {
		final Document document = parseXmlFile(unformattedXml);

//		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
		Transformer transformer = transformerFactory.newTransformer();
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);
		String xmlString = result.getWriter().toString();
		return xmlString;
	}
}
