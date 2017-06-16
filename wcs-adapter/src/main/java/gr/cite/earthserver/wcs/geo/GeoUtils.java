package gr.cite.earthserver.wcs.geo;

import com.vividsolutions.jts.geom.Polygon;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.core.utils.Pair;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GeoUtils {
	private static final String DEFAULT_CRS = "EPSG:4326";

	public static Pair<String, String> getGeoJsonBoundingBoxFromDescribeCoverage(String describeCoverageXML) throws ParseException {
		/*String boundingBoxJSON;
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));



			CoordinateReferenceSystem currentCrs;
			try {
				currentCrs = CRS.decode(crsString);
			} catch(NoSuchAuthorityCodeException e) {
				throw new ParseException(e.getMessage(), e);
			}

			List<String> axisLabels = GeoUtils.getAxisLabels(xPathEvaluator);
			List<String> lowerCorners = GeoUtils.getLowerCorners(xPathEvaluator);
			List<String> upperCorners = GeoUtils.getUpperCorners(xPathEvaluator);


			Double lowerCornerLat = 0.0;
			Double lowerCornerLon = 0.0;
			Double upperCornerLat = 0.0;
			Double upperCornerLon = 0.0;
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
							lowerCornerLat = lCornerLat;

							double uCornerLat = Double.parseDouble(upperCorner[i]);
							if (uCornerLat > 90.0) {
								uCornerLat = Math.floor(uCornerLat);
							} else if (lCornerLat < -90.0) {
								uCornerLat = Math.ceil(uCornerLat);
							}
							upperCornerLat = uCornerLat;

							break;

						case "long":

							double lCornerLong = Double.parseDouble(lowerCorner[i]);
							if (lCornerLong > 180.0) {
								lCornerLong = Math.floor(lCornerLong);
							} else if (lCornerLong < -180.0) {
								lCornerLong = Math.ceil(lCornerLong);
							}
							lowerCornerLon = lCornerLong;

							double uCornerLong = Double.parseDouble(upperCorner[i]);
							if (uCornerLong > 180.0) {
								uCornerLong = Math.floor(uCornerLong);
							} else if (uCornerLong < -180.0) {
								uCornerLong = Math.ceil(uCornerLong);
							}
							upperCornerLon = uCornerLong;

							break;

						case "e":

							lowerCornerLon = Double.parseDouble(lowerCorner[i]);
							upperCornerLon = Double.parseDouble(upperCorner[i]);

							break;

						case "n":

							lowerCornerLat = Double.parseDouble(lowerCorner[i]);
							upperCornerLat = Double.parseDouble(upperCorner[i]);

							break;
						default:
							break;
					}
				}
			}

			CoordinateReferenceSystem defaultCrs = CRS.decode(GeoUtils.DEFAULT_CRS);
			ReferencedEnvelope envelope;
			try {
				envelope = new ReferencedEnvelope(lowerCornerLon, upperCornerLon, lowerCornerLat, upperCornerLat, currentCrs);
			} catch(MismatchedDimensionException e) {
				throw new ParseException(e);
			}

			if (!currentCrs.getName().equals(defaultCrs.getName())) {
				envelope = envelope.transform(defaultCrs, true);
			}
			Polygon geometry = JTS.toGeometry(envelope);

			GeometryJSON geomJSON = new GeometryJSON();
			boundingBoxJSON = geomJSON.toString(geometry);


		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException |
				MalformedURLException | MismatchedDimensionException | FactoryException | TransformException e) {
			throw new ParseException(e);
		}

		return new Pair<>(GeoUtils.DEFAULT_CRS, boundingBoxJSON);*/
		return null;
	}

	private static List<Axis> getAxes(XPathEvaluator evaluator) {
		return null;
	}

	static List<String> getCrs(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> crs = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsName");
		return Arrays.stream(URI.create(crs.stream().findFirst().orElse("")).getQuery().split("&"))
				.map(queryParam -> queryParam.substring(queryParam.indexOf("/crs/") + 5, queryParam.indexOf("/crs/") + 9) + ":" + queryParam.substring(queryParam.lastIndexOf("/") + 1))
				.collect(Collectors.toList());
	}

	static List<String> getAxisLabels(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> axisLabels = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@axisLabels");
		return Arrays.stream(axisLabels.stream().findFirst().orElse("").split(" ")).map(String::toLowerCase).collect(Collectors.toList());
	}

	static List<String> getLowerCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> lowerCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='lowerCorner']/text()");
		return Arrays.asList(lowerCorners.stream().findFirst().orElse("").split(" "));
	}

	static List<String> getUpperCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> upperCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='upperCorner']/text()");
		return Arrays.asList(upperCorners.stream().findFirst().orElse("").split(" "));
	}

	public static void main(String[] args) throws ParseException, NoSuchAuthorityCodeException, FactoryException, XMLConversionException, XPathFactoryConfigurationException, XPathEvaluationException {

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("http://eodataservice.org/rasdaman/ows");

		String describeCoverageXML = webTarget
				.queryParam("service", "WCS")
				.queryParam("version", "2.0.1")
				.queryParam("request", "DescribeCoverage")
				.queryParam("coverageId", "L8_B5_32634_30")
				.request().get(String.class);

		//System.out.println(WCSParseUtils.getBoundingBoxJSON(xml));
		//System.out.println(CRS.decode("EPSG:4326").getName());
		List<String> labels = GeoUtils.getAxisLabels(new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true)));
		System.out.println(labels);

		List<String> crs = GeoUtils.getCrs(new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true)));
		System.out.println(crs);
	}
}
