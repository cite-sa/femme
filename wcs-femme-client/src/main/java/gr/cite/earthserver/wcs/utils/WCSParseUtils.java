package gr.cite.earthserver.wcs.utils;

import java.util.List;

import javax.xml.xpath.XPathFactoryConfigurationException;

import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;

public class WCSParseUtils {

	/**
	 * 
	 * @param value
	 * @return the list of coverage IDs in the specified get capabilities
	 *         response
	 * @throws ParseException 
	 */
	public static List<String> getCoverageIds(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			return xPathEvaluator.evaluate("//wcs:CoverageSummary/wcs:CoverageId/text()");
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getServerName(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			return xPathEvaluator.evaluate("//wcs:Capabilities/ows:ServiceIdentification/ows:Title/text()").get(0);
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getCoverageId(String describeCoverageXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			return xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/wcs:CoverageId/text()").get(0);
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		}
	}
}
