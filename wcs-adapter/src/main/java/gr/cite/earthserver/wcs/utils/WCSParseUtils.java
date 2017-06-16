package gr.cite.earthserver.wcs.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

import gr.cite.femme.core.utils.Pair;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;

public final class WCSParseUtils {

	private static final Logger logger = LoggerFactory.getLogger(WCSParseUtils.class);
	
	private WCSParseUtils() {
		
	}

	public static List<String> getCoverageIds(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			List<String> coverageIds = xPathEvaluator.evaluate("/wcs:Capabilities/wcs:Contents/wcs:CoverageSummary/wcs:CoverageId/text()");
			logger.info("Total coverages: " + coverageIds.size());
			return coverageIds;
		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getServerName(String getCapabilitiesXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(getCapabilitiesXML, true));
			return xPathEvaluator.evaluate("/wcs:Capabilities/ows:ServiceIdentification/ows:Title/text()").get(0);
		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
	
	public static String getCoverageId(String describeCoverageXML) throws ParseException {
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			return xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/wcs:CoverageId/text()").get(0);
		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException e) {
			throw new ParseException(e);
		}
	}
}
