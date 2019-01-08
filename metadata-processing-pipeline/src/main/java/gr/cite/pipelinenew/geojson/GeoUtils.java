package gr.cite.pipelinenew.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import org.geojson.GeoJsonObject;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GeoUtils {
	private static final Logger logger = LoggerFactory.getLogger(GeoUtils.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static final String DEFAULT_CRS = "EPSG:4326";
	
	public static Pair<String, String> getGeoJsonBoundingBoxFromDescribeCoverage(String describeCoverageXML) throws ParseException {
		String boundingBoxJSON;
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			
			List<Axis> axes = GeoUtils.getAxes(xPathEvaluator);
			
			String currentCrsString = axes.stream().filter(GeoUtils::isLongitude).map(Axis::getCrs).findFirst().orElseThrow(() -> new ParseException(""));
			
			logger.info("CRS: " + currentCrsString);
			CoordinateReferenceSystem defaultCrs = CRS.decode(GeoUtils.DEFAULT_CRS);
			CoordinateReferenceSystem currentCrs = CRS.decode(currentCrsString, true);
			
			logger.info(currentCrs.getName() + " - " + defaultCrs.getName() + " --- " + currentCrs.getName().equals(defaultCrs.getName()));
			
			ReferencedEnvelope referencedEnvelope;
			Double lowerCornerLong = axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""));
			Double lowerCornerLat = axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""));
			Double upperCornerLong = axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""));
			Double upperCornerLat = axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""));
			
			if (currentCrs.getName().equals(defaultCrs.getName())) {
				referencedEnvelope = new ReferencedEnvelope(
					validateLongitude(lowerCornerLong),
					validateLongitude(upperCornerLong),
					validateLatitude(lowerCornerLat),
					validateLatitude(upperCornerLat),
					currentCrs
				);
			} else {
				referencedEnvelope = new ReferencedEnvelope(lowerCornerLong, upperCornerLong, lowerCornerLat, upperCornerLat, currentCrs);
			}

			Envelope envelope =  referencedEnvelope;
			if (!currentCrs.getName().equals(defaultCrs.getName())) {
				
				MathTransform transform = CRS.findMathTransform(currentCrs, defaultCrs, false);
				DirectPosition sourceLowerCorner = referencedEnvelope.getLowerCorner();
				DirectPosition sourceUpperCorner = referencedEnvelope.getUpperCorner();
				
				DirectPosition2D targetLowerCorner = new DirectPosition2D(defaultCrs);
				DirectPosition2D targetUpperCorner = new DirectPosition2D(defaultCrs);
				
				double[] sourceCoordinates = new double[] {lowerCornerLong, lowerCornerLat, upperCornerLong, upperCornerLat};
				double[] targetCoordinates = new double[4];
				/*transform.transform(sourceLowerCorner, targetLowerCorner);
				transform.transform(sourceUpperCorner, targetUpperCorner);*/
				transform.transform(sourceCoordinates, 0, targetCoordinates, 0, 2);
				
				/*envelope = new ReferencedEnvelope(
					validateLatitude((targetLowerCorner).getX()),
					validateLatitude((targetUpperCorner).getX()),
					validateLongitude((targetLowerCorner).getY()),
					validateLongitude((targetUpperCorner).getY()),
					defaultCrs
				);*/
				
				envelope = new ReferencedEnvelope(
					validateLongitude(targetCoordinates[1]),
					validateLongitude(targetCoordinates[3]),
					validateLatitude(targetCoordinates[0]),
					validateLatitude(targetCoordinates[2]),
					defaultCrs
				);
				
				
				//envelope = JTS.transform(envelope, transform);
				//Envelope better = JTS.transform(envelope, null, transform, 10);
			}
			
			Polygon geometry = JTS.toGeometry(envelope);
			GeometryJSON geomJSON = new GeometryJSON();
			boundingBoxJSON = geomJSON.toString(geometry);
			
		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException | MismatchedDimensionException | FactoryException | TransformException e) {
			throw new ParseException(e);
		}
		
		logger.debug(boundingBoxJSON);
		
		
		return new Pair<>(GeoUtils.DEFAULT_CRS, boundingBoxJSON);
	}
	
	private static Double validateLongitude(Double longitude) {
		if (longitude < - 180.0) {
			return - 180.0;
		} else if (longitude > 180.0) {
			return 180.0;
		}
		return longitude;
	}
	
	private static Double validateLatitude(Double latitude) {
		if (latitude < - 90.0) {
			return - 90.0;
		} else if (latitude > 90.0) {
			return 90.0;
		}
		return latitude;
	}
	
	private static boolean isLatitude(String axisLabel) {
		return "lat".equals(axisLabel.toLowerCase()) || "n".equals(axisLabel.toLowerCase());
	}
	
	private static boolean isLatitude(Axis axis) {
		return "lat".equals(axis.getLabel()) || "n".equals(axis.getLabel());
	}
	
	private static boolean isLongitude(String axisLabel) {
		return "long".equals(axisLabel.toLowerCase()) || "e".equals(axisLabel.toLowerCase());
	}
	
	private static boolean isLongitude(Axis axis) {
		return "long".equals(axis.getLabel()) || "e".equals(axis.getLabel());
	}
	
	private static List<Axis> getAxes(XPathEvaluator evaluator) throws XPathEvaluationException, ParseException {
		int crsDimension = GeoUtils.getCrsDimension(evaluator);
		List<String> axisLabels = GeoUtils.getAxisLabels(evaluator);
		List<String> crs = GeoUtils.getCrs(evaluator);
		List<String> lowerCorners = GeoUtils.getLowerCorners(evaluator);
		List<String> upperCorners = GeoUtils.getUpperCorners(evaluator);
		
		List<Axis> axes = new ArrayList<>();
		for (int axesIndex = 0; axesIndex < crsDimension; axesIndex++) {
			if (isLatitude(axisLabels.get(axesIndex)) || isLongitude(axisLabels.get(axesIndex))) {
				axes.add(new Axis(axisLabels.get(axesIndex),
						crs.stream().filter(crsString -> crsString.startsWith("EPSG")).findFirst().orElseThrow(() -> new ParseException("No valid CRS")),
						lowerCorners.get(axesIndex), upperCorners.get(axesIndex)));
			}
		}
		
		return axes;
	}
	
	private static int getCrsDimension(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> crsDimension = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsDimension");
		return crsDimension.stream().map(Integer::parseInt).findFirst().orElse(0);
	}
	
	private static List<String> getCrs(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> crs = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@srsName");
		
		String crsString = crs.stream().findFirst().orElse("");
		
		Stream<String> crsStrings;
		int index = crsString.indexOf("?");
		if (index < 0) {
			crsStrings = Stream.of(crsString);
		} else {
			String segment = crsString.substring(index, crsString.length());
			crsStrings = Arrays.stream(segment.split("[?&][0-9]=")).filter(crsParam -> ! Strings.isNullOrEmpty(crsParam));
		}
		
		return crsStrings.map(queryParam -> queryParam.substring(queryParam.indexOf("/crs/") + 5, queryParam.indexOf("/0/")) + ":" + queryParam.substring(queryParam.lastIndexOf("/") + 1))
					   .collect(Collectors.toList());
	}
	
	private static List<String> getAxisLabels(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> axisLabels = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/@axisLabels");
		return Arrays.stream(axisLabels.stream().findFirst().orElse("").split(" ")).map(String::toLowerCase).collect(Collectors.toList());
	}
	
	private static List<String> getLowerCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> lowerCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='lowerCorner']/text()");
		return Arrays.stream(lowerCorners.stream().findFirst().orElse("").split(" ")).collect(Collectors.toList());
	}
	
	private static List<String> getUpperCorners(XPathEvaluator evaluator) throws XPathEvaluationException {
		List<String> upperCorners = evaluator.evaluate("/wcs:CoverageDescriptions/wcs:CoverageDescription/*[local-name()='boundedBy']/*[local-name()='Envelope']/*[local-name()='upperCorner']/text()");
		return Arrays.stream(upperCorners.stream().findFirst().orElse("").split(" ")).collect(Collectors.toList());
	}
	
	/***************************************** Geo Transformations *****************************************/
	/*public static CoverageGeo convertDataToCoverageGeo(String coverageDescription) throws ParseException, IOException {
		CoverageGeo coverageGeo = new CoverageGeo();
		
		Pair<String, String> crsAndGeometry = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(coverageDescription);
		GeoJsonObject object = mapper.readValue(crsAndGeometry.getRight(), GeoJsonObject.class);
		
		coverageGeo.setDataElementId(dataElement.getId());
		coverageGeo.setName(dataElement.getName());
		coverageGeo.setServerId(serverId);
		coverageGeo.setName(dataElement.getName());
		coverageGeo.setCrs(crsAndGeometry.getLeft());
		coverageGeo.setGeo(object);
		
		return coverageGeo;
	}*/
}
