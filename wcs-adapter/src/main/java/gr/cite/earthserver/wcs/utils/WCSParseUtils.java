package gr.cite.earthserver.wcs.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeometryFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import gr.cite.earthserver.wcs.core.BoundingBox;
import gr.cite.femme.utils.Pair;
import gr.cite.scarabaeus.utils.xml.XMLConverter;
import gr.cite.scarabaeus.utils.xml.XPathEvaluator;
import gr.cite.scarabaues.utils.xml.exceptions.XMLConversionException;
import gr.cite.scarabaues.utils.xml.exceptions.XPathEvaluationException;

public final class WCSParseUtils {
	
	private WCSParseUtils() {
		
	}

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
	
	public static Pair<String, String> getBoundingBoxJSON(String describeCoverageXML) throws ParseException {
		String boundingBoxJSON = null;
		String defaultCrsName = "EPSG:4326";
			
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			
			List<String> xPathResults = null;
			
			String crsString = null;
			xPathResults = xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsName");
			crsString = xPathResults.size() > 0 ? xPathResults.get(0) : null;
			if (crsString != null) {
				URL srsUrl = new URL(crsString);
				if (srsUrl.getQuery() != null) {
					String[] srsQueryParams = srsUrl.getQuery().split("&");
					String srsQueryParam = srsQueryParams.length > 0 ? srsQueryParams[0] : null;
					if (srsQueryParam != null) {
						crsString = srsQueryParam.replace("1=", "");
						crsString = crsString.substring(crsString.indexOf("/crs/") + 5, crsString.indexOf("/crs/") + 9) + ":" + crsString.substring(crsString.lastIndexOf("/") + 1);
	
					}
				} else {
					crsString = srsUrl.toString();
					crsString = crsString.substring(crsString.indexOf("/crs/") + 5, crsString.indexOf("/crs/") + 9) + ":" + crsString.substring(crsString.lastIndexOf("/") + 1);
				}
			}
			
			CoordinateReferenceSystem currentCrs = null;
			try {
				currentCrs = CRS.decode(crsString);
			} catch(NoSuchAuthorityCodeException e) {
				throw new ParseException(e.getMessage(), e);
			}
			
			String axisLabelsString = null;
			String[] axisLabels = null;
			xPathResults = xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@axisLabels");
			axisLabelsString = xPathResults.size() > 0 ? xPathResults.get(0) : null;
			if (axisLabelsString != null) {
				axisLabels = axisLabelsString.split(" ");				
			}
			
			String lowerCornerString = null;
			xPathResults = xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='lowerCorner']/text()");
			lowerCornerString = xPathResults.size() > 0 ? xPathResults.get(0) : null;
			String[] lowerCorner = lowerCornerString.split(" ");
			
			String upperCornerString = null;
			xPathResults = xPathEvaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='upperCorner']/text()");
			upperCornerString = xPathResults.size() > 0 ? xPathResults.get(0) : null;
			String[] upperCorner = upperCornerString.split(" ");
			
			
			Double lowerCornerLatitude = 0.0;
			Double lowerCornerLongtitude = 0.0;
			Double upperCornerLatitude = 0.0;
			Double upperCornerLongtitude = 0.0;
			if (axisLabels != null) {
				for (int i = 0 ; i < axisLabels.length; i ++) {
					switch (axisLabels[i].toLowerCase()) {
					case "lat":
						
						double lCornerLat = Double.parseDouble(lowerCorner[i]);
						if (lCornerLat > 90.0) {
							lCornerLat = Math.floor(lCornerLat);
						} else if (lCornerLat < -90.0) {
							lCornerLat = Math.ceil(lCornerLat);
						}
						lowerCornerLatitude = lCornerLat;
						
						double uCornerLat = Double.parseDouble(upperCorner[i]);
						if (uCornerLat > 90.0) {
							uCornerLat = Math.floor(uCornerLat);
						} else if (lCornerLat < -90.0) {
							uCornerLat = Math.ceil(uCornerLat);
						}
						upperCornerLatitude = uCornerLat;
						
						break;
						
					case "long":
						
						double lCornerLong = Double.parseDouble(lowerCorner[i]);
						if (lCornerLong > 180.0) {
							lCornerLong = Math.floor(lCornerLong);
						} else if (lCornerLong < -180.0) {
							lCornerLong = Math.ceil(lCornerLong);
						}
						lowerCornerLongtitude = lCornerLong;
						
						double uCornerLong = Double.parseDouble(upperCorner[i]);
						if (uCornerLong > 180.0) {
							uCornerLong = Math.floor(uCornerLong);
						} else if (uCornerLong < -180.0) {
							uCornerLong = Math.ceil(uCornerLong);
						}
						upperCornerLongtitude = uCornerLong;
						
						break;
						
					case "e":
						
						lowerCornerLongtitude = Double.parseDouble(lowerCorner[i]);
						upperCornerLongtitude = Double.parseDouble(upperCorner[i]);
						
						break;
						
					case "n":
						
						lowerCornerLatitude = Double.parseDouble(lowerCorner[i]);
						upperCornerLatitude = Double.parseDouble(upperCorner[i]);
						
						break;
					default:
						break;
					}
					
				}
			}
			
			CoordinateReferenceSystem defaultCrs = CRS.decode("EPSG:4326");
			//CoordinateReferenceSystem currentCrs = CRS.decode(crsString);
			ReferencedEnvelope envelope = null;
			try {
				envelope = new ReferencedEnvelope(lowerCornerLongtitude, upperCornerLongtitude, lowerCornerLatitude, upperCornerLatitude, currentCrs);
			} catch(MismatchedDimensionException e) {
				throw new ParseException(e);				
			}
		    
		    if (!currentCrs.getName().equals(defaultCrs.getName())) {		    	
		    	envelope = envelope.transform(defaultCrs, true);
		    }
		    Polygon geometry = JTS.toGeometry(envelope);
		    
		    GeometryJSON geomJSON = new GeometryJSON();
		    boundingBoxJSON = geomJSON.toString(geometry);
			
			
		} catch (XPathFactoryConfigurationException e) {
			throw new ParseException(e);
		} catch (XMLConversionException e) {
			throw new ParseException(e);
		} catch (XPathEvaluationException e) {
			throw new ParseException(e);
		} catch (MalformedURLException e) {
			throw new ParseException(e);
		} catch (MismatchedDimensionException e) {
			throw new ParseException(e);
		} catch (NoSuchAuthorityCodeException e) {
			throw new ParseException(e);
		} catch (FactoryException e) {
			throw new ParseException(e);
		} catch (TransformException e) {
			throw new ParseException(e);
		}
		
		return new Pair<String, String>(defaultCrsName, boundingBoxJSON);
	}
	
	public static void main(String[] args) throws ParseException, NoSuchAuthorityCodeException, FactoryException {
		
		Client client = ClientBuilder.newClient();
		 WebTarget webTarget = client.target("http://earthserver.ecmwf.int/rasdaman/ows");
		 
		 String xml = webTarget
				 .queryParam("service", "WCS")
				 .queryParam("version", "2.0.1")
				 .queryParam("request", "DescribeCoverage")
				 .queryParam("coverageId", "mslp")
				 .request().get(String.class);
		 
		 //System.out.println(WCSParseUtils.getBoundingBoxJSON(xml));
		 System.out.println(CRS.decode("EPSG:4326").getName());
	}
}
