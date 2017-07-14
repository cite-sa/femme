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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GeoUtils {
	private static final String DEFAULT_CRS = "EPSG:4326";

	public static Pair<String, String> getGeoJsonBoundingBoxFromDescribeCoverage(String describeCoverageXML) throws ParseException {
		String boundingBoxJSON;
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			List<Axis> axes = GeoUtils.getAxes(xPathEvaluator);

			CoordinateReferenceSystem defaultCrs = CRS.decode(GeoUtils.DEFAULT_CRS);
			CoordinateReferenceSystem currentCrs = CRS.decode(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getCrs).findFirst().orElseThrow(() -> new ParseException("")));
			ReferencedEnvelope envelope;
			try {
				envelope = new ReferencedEnvelope(
						axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")),
						axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")),
						axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")),
						axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")),
						currentCrs
				);
			} catch(MismatchedDimensionException e) {
				throw new ParseException(e);
			}

			if (!currentCrs.getName().equals(defaultCrs.getName())) {
				envelope = envelope.transform(defaultCrs, true);
			}
			Polygon geometry = JTS.toGeometry(envelope);

			GeometryJSON geomJSON = new GeometryJSON();
			boundingBoxJSON = geomJSON.toString(geometry);

		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException | MismatchedDimensionException | FactoryException | TransformException e) {
			throw new ParseException(e);
		}

		return new Pair<>(GeoUtils.DEFAULT_CRS, boundingBoxJSON);
	}

	private static boolean isLatitude(Axis axis) {
		return "lat".equals(axis.getLabel()) || "n".equals(axis.getLabel());
	}

	private static boolean isLongitude(Axis axis) {
		return "long".equals(axis.getLabel()) || "e".equals(axis.getLabel());
	}

	private static List<Axis> getAxes(XPathEvaluator evaluator) throws XPathEvaluationException {
		int crsDimension = GeoUtils.getCrsDimension(evaluator);
		List<String> axisLabels = GeoUtils.getAxisLabels(evaluator);
		List<String> crs = GeoUtils.getCrs(evaluator);
		List<Double> lowerCorners = GeoUtils.getLowerCorners(evaluator);
		List<Double> upperCorners = GeoUtils.getUpperCorners(evaluator);

		List<Axis> axes = new ArrayList<>();
		for (int axesIndex = 0; axesIndex < crsDimension; axesIndex++) {
			axes.add(new Axis(axisLabels.get(axesIndex), crs.get(0), lowerCorners.get(axesIndex), upperCorners.get(axesIndex)));
		}

		return axes;
	}

	private static int getCrsDimension(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> crsDimension = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsDimension");
		return crsDimension.stream().map(Integer::parseInt).findFirst().orElse(0);
	}

	private static List<String> getCrs(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> crs = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsName");
		return Arrays.stream(URI.create(crs.stream().findFirst().orElse("")).getQuery().split("&"))
				.map(queryParam -> queryParam.substring(queryParam.indexOf("/crs/") + 5, queryParam.indexOf("/0/")) + ":" + queryParam.substring(queryParam.lastIndexOf("/") + 1))
				.collect(Collectors.toList());
	}

	private static List<String> getAxisLabels(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> axisLabels = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@axisLabels");
		return Arrays.stream(axisLabels.stream().findFirst().orElse("").split(" ")).map(String::toLowerCase).collect(Collectors.toList());
	}

	private static List<Double> getLowerCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> lowerCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='lowerCorner']/text()");
		return Arrays.stream(lowerCorners.stream().findFirst().orElse("").split(" ")).map(Double::parseDouble).collect(Collectors.toList());
	}

	private static List<Double> getUpperCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> upperCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='upperCorner']/text()");
		return Arrays.stream(upperCorners.stream().findFirst().orElse("").split(" ")).map(Double::parseDouble).collect(Collectors.toList());
	}

	public static void main(String[] args) throws ParseException {

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("http://eodataservice.org/rasdaman/ows");

		String describeCoverageXML = webTarget
				.queryParam("service", "WCS")
				.queryParam("version", "2.0.1")
				.queryParam("request", "DescribeCoverage")
				.queryParam("coverageId", "L8_B5_32634_30")
				.request().get(String.class);

		Pair<String, String> geoJson = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(describeCoverageXML);
		System.out.println(geoJson.getLeft());
		System.out.println(geoJson.getRight());
	}
}
