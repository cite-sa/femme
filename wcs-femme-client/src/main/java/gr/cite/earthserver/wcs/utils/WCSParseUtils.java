package gr.cite.earthserver.wcs.utils;

import java.util.List;

import javax.xml.xpath.XPathFactoryConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;
import gr.cite.scarabaues.utils.xml.exceptions.XMLConversionException;
import gr.cite.scarabaues.utils.xml.exceptions.XPathEvaluationException;

public class WCSParseUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(WCSParseUtils.class); 

	public static List<String> getCoverageIds(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			return xPathEvaluator.evaluate("/wcs:Capabilities/wcs:Contents/wcs:CoverageSummary/wcs:CoverageId/text()");
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		} catch (XMLConversionException e) {
			throw new ParseException(e);
		} catch (XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getServerName(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			return xPathEvaluator.evaluate("/wcs:Capabilities/ows:ServiceIdentification/ows:Title/text()").get(0);
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		} catch (XMLConversionException e) {
			throw new ParseException(e);
		} catch (XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getCoverageId(String describeCoverageXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			return xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/wcs:CoverageId/text()").get(0);
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		} catch (XMLConversionException e) {
			throw new ParseException(e);
		} catch (XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
}
