package gr.cite.earthserver.wcs.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.utils.Pair;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
			if (currentCrs.getName().equals(defaultCrs.getName())) {
				referencedEnvelope = new ReferencedEnvelope(
					validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
					validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
					validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
					validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
					currentCrs
				);
			} else {
				System.out.println(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")));
				System.out.println(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")));
				System.out.println(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")));
				System.out.println(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")));
				
				referencedEnvelope = new ReferencedEnvelope(
					axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")),
					axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")),
					axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")),
					axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException("")),
					currentCrs
				);
			}

			Envelope envelope =  referencedEnvelope;
			if (!currentCrs.getName().equals(defaultCrs.getName())) {
				
				MathTransform transform = CRS.findMathTransform(currentCrs, defaultCrs, false);
				DirectPosition sourceLowerCorner = referencedEnvelope.getLowerCorner();
				DirectPosition sourceUpperCorner = referencedEnvelope.getUpperCorner();
				
				DirectPosition targetLowerCorner = new DirectPosition2D();
				DirectPosition targetUpperCorner = new DirectPosition2D();
				
				transform.transform(sourceLowerCorner, targetLowerCorner);
				transform.transform(sourceUpperCorner, targetUpperCorner);
				
				envelope = new ReferencedEnvelope(
					validateLatitude(((DirectPosition2D) targetLowerCorner).getX()),
					validateLongitude(((DirectPosition2D) targetLowerCorner).getY()),
					validateLatitude(((DirectPosition2D) targetUpperCorner).getX()),
						validateLongitude(((DirectPosition2D) targetUpperCorner).getY()),
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
	
	/*************************************Geo Transformations**************************************************************************/
	public static CoverageGeo convertDataToCoverageGeo(WCSResponse coverage, String serverId, DataElement dataElement) throws ParseException, IOException {
		CoverageGeo coverageGeo = new CoverageGeo();
		
		Pair<String, String> crsAndGeometry = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(coverage.getResponse());
		GeoJsonObject object = mapper.readValue(crsAndGeometry.getRight(), GeoJsonObject.class);
		
		coverageGeo.setDataElementId(dataElement.getId());
		coverageGeo.setServerId(serverId);
		coverageGeo.setCoverageName(dataElement.getName());
		coverageGeo.setCrs(crsAndGeometry.getLeft());
		coverageGeo.setGeo(object);
		
		return coverageGeo;
	}
	
	public static void main(String[] args) throws ParseException, KeyManagementException, NoSuchAlgorithmException, IOException, FemmeException {
		String dc = "<wcs:CoverageDescriptions xsi:schemaLocation=\"http://www.opengis.net/wcs/2.0 http://schemas.opengis.net/wcs/2.0/wcsAll.xsd\" xmlns:wcs=\"http://www.opengis.net/wcs/2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wcscrs=\"http://www.opengis.net/wcs/service-extension/crs/1.0\" xmlns:ows=\"http://www.opengis.net/ows/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
						"  <wcs:CoverageDescription gml:id=\"LS8_test_tile2\" xmlns=\"http://www.opengis.net/gml/3.2\" xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\" xmlns:swe=\"http://www.opengis.net/swe/2.0\">\n" +
						"    <boundedBy>\n" +
						"      <Envelope srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/OGC/0/UnixTime&amp;2=http://localhost:8080/def/crs/EPSG/0/3577\" axisLabels=\"unix E N\" uomLabels=\"s metre metre\" srsDimension=\"3\">\n" +
						"        <lowerCorner>\"2013-04-15T01:50:32.330Z\" -1000000 -1700000</lowerCorner>\n" +
						"        <upperCorner>\"2013-12-18T01:56:23.170Z\" -900000 -1600000</upperCorner>\n" +
						"      </Envelope>\n" +
						"    </boundedBy>\n" +
						"    <wcs:CoverageId>LS8_test_tile2</wcs:CoverageId>\n" +
						"    <coverageFunction>\n" +
						"      <GridFunction>\n" +
						"        <sequenceRule axisOrder=\"+3 +2 +1\">Linear</sequenceRule>\n" +
						"        <startPoint>0 0 0</startPoint>\n" +
						"      </GridFunction>\n" +
						"    </coverageFunction>\n" +
						"    <gmlcov:metadata>\n" +
						"      <gmlcov:Extension>\n" +
						"        <source>This data is a reprojection and retile of Landsat surface reflectance scene data.</source>\n" +
						"        <history>NetCDF-CF file created by datacube version '1.0.2' at 20160412.</history>\n" +
						"        <title>Experimental Data files From the Australian Geoscience Data Cube - DO NOT USE</title>\n" +
						"        <date_created>2016-04-12T10:47:36.525376</date_created>\n" +
						"        <product_version>0.0.0</product_version>\n" +
						"        <summary>These files are experimental, short lived, and the format will change.</summary>\n" +
						"        <Conventions>CF-1.6, ACDD-1.3</Conventions>\n" +
						"        <slices/>\n" +
						"      </gmlcov:Extension>\n" +
						"    </gmlcov:metadata>\n" +
						"    <domainSet>\n" +
						"      <gmlrgrid:ReferenceableGridByVectors dimension=\"3\" gml:id=\"LS8_test_tile2-grid\" xsi:schemaLocation=\"http://www.opengis.net/gml/3.3/rgrid http://schemas.opengis.net/gml/3.3/referenceableGrid.xsd\" xmlns:gmlrgrid=\"http://www.opengis.net/gml/3.3/rgrid\">\n" +
						"        <limits>\n" +
						"          <GridEnvelope>\n" +
						"            <low>0 0 0</low>\n" +
						"            <high>27 3999 3999</high>\n" +
						"          </GridEnvelope>\n" +
						"        </limits>\n" +
						"        <axisLabels>unix E N</axisLabels>\n" +
						"        <gmlrgrid:origin>\n" +
						"          <Point gml:id=\"LS8_test_tile2-origin\" srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/OGC/0/UnixTime&amp;2=http://localhost:8080/def/crs/EPSG/0/3577\">\n" +
						"            <pos>\"2013-04-15T01:50:32.330Z\" -999987.5 -1600012.5</pos>\n" +
						"          </Point>\n" +
						"        </gmlrgrid:origin>\n" +
						"        <gmlrgrid:generalGridAxis>\n" +
						"          <gmlrgrid:GeneralGridAxis>\n" +
						"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/OGC/0/UnixTime&amp;2=http://localhost:8080/def/crs/EPSG/0/3577\">1 0 0</gmlrgrid:offsetVector>\n" +
						"            <gmlrgrid:coefficients>\"2013-04-15T01:50:32.330Z\" \"2013-05-01T01:50:30.160Z\" \"2013-05-24T01:56:56.470Z\" \"2013-06-02T01:50:46.090Z\" \"2013-06-09T01:56:55.080Z\" \"2013-06-18T01:50:39.680Z\" \"2013-06-25T01:56:49.800Z\" \"2013-07-04T01:50:41.720Z\" \"2013-07-11T01:56:53.120Z\" \"2013-07-20T01:50:40.490Z\" \"2013-07-27T01:56:52.760Z\" \"2013-08-05T01:50:43.590Z\" \"2013-08-12T01:56:54.260Z\" \"2013-08-21T01:50:21.030Z\" \"2013-08-21T01:50:44.970Z\" \"2013-08-28T01:56:56.710Z\" \"2013-09-13T01:56:53.350Z\" \"2013-09-22T01:50:37.940Z\" \"2013-10-08T01:50:36.180Z\" \"2013-10-15T01:56:45.580Z\" \"2013-10-24T01:50:05.480Z\" \"2013-10-24T01:50:29.410Z\" \"2013-10-31T01:56:37.340Z\" \"2013-11-09T01:50:27.490Z\" \"2013-11-16T01:56:35.210Z\" \"2013-12-02T01:56:31.190Z\" \"2013-12-11T01:50:18.280Z\" \"2013-12-18T01:56:23.170Z\"</gmlrgrid:coefficients>\n" +
						"            <gmlrgrid:gridAxesSpanned>unix</gmlrgrid:gridAxesSpanned>\n" +
						"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>\n" +
						"          </gmlrgrid:GeneralGridAxis>\n" +
						"        </gmlrgrid:generalGridAxis>\n" +
						"        <gmlrgrid:generalGridAxis>\n" +
						"          <gmlrgrid:GeneralGridAxis>\n" +
						"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/OGC/0/UnixTime&amp;2=http://localhost:8080/def/crs/EPSG/0/3577\">0 25 0</gmlrgrid:offsetVector>\n" +
						"            <gmlrgrid:coefficients/>\n" +
						"            <gmlrgrid:gridAxesSpanned>E</gmlrgrid:gridAxesSpanned>\n" +
						"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>\n" +
						"          </gmlrgrid:GeneralGridAxis>\n" +
						"        </gmlrgrid:generalGridAxis>\n" +
						"        <gmlrgrid:generalGridAxis>\n" +
						"          <gmlrgrid:GeneralGridAxis>\n" +
						"            <gmlrgrid:offsetVector srsName=\"http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/OGC/0/UnixTime&amp;2=http://localhost:8080/def/crs/EPSG/0/3577\">0 0 -25</gmlrgrid:offsetVector>\n" +
						"            <gmlrgrid:coefficients/>\n" +
						"            <gmlrgrid:gridAxesSpanned>N</gmlrgrid:gridAxesSpanned>\n" +
						"            <gmlrgrid:sequenceRule axisOrder=\"+1\">Linear</gmlrgrid:sequenceRule>\n" +
						"          </gmlrgrid:GeneralGridAxis>\n" +
						"        </gmlrgrid:generalGridAxis>\n" +
						"      </gmlrgrid:ReferenceableGridByVectors>\n" +
						"    </domainSet>\n" +
						"    <gmlcov:rangeType>\n" +
						"      <swe:DataRecord>\n" +
						"        <swe:field name=\"band_1\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_2\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_3\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_4\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_5\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_6\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"        <swe:field name=\"band_7\">\n" +
						"          <swe:Quantity>\n" +
						"            <swe:nilValues>\n" +
						"              <swe:NilValues>\n" +
						"                <swe:nilValue reason=\"\">-999</swe:nilValue>\n" +
						"              </swe:NilValues>\n" +
						"            </swe:nilValues>\n" +
						"            <swe:uom code=\"10^0\"/>\n" +
						"          </swe:Quantity>\n" +
						"        </swe:field>\n" +
						"      </swe:DataRecord>\n" +
						"    </gmlcov:rangeType>\n" +
						"    <wcs:ServiceParameters>\n" +
						"      <wcs:CoverageSubtype>ReferenceableGridCoverage</wcs:CoverageSubtype>\n" +
						"      <CoverageSubtypeParent xmlns=\"http://www.opengis.net/wcs/2.0\">\n" +
						"        <CoverageSubtype>AbstractDiscreteCoverage</CoverageSubtype>\n" +
						"        <CoverageSubtypeParent>\n" +
						"          <CoverageSubtype>AbstractCoverage</CoverageSubtype>\n" +
						"        </CoverageSubtypeParent>\n" +
						"      </CoverageSubtypeParent>\n" +
						"      <wcs:nativeFormat>application/octet-stream</wcs:nativeFormat>\n" +
						"    </wcs:ServiceParameters>\n" +
						"  </wcs:CoverageDescription>\n" +
						"</wcs:CoverageDescriptions>";
		Pair<String, String> bbox = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(dc);
		System.out.println(bbox.getRight());
		
		/*SSLContext sslcontext = SSLContext.getInstance("TLS");
		
		sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
			
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
			
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}}, new java.security.SecureRandom());
		
		Client client = ClientBuilder.newBuilder()
								.sslContext(sslcontext)
								.hostnameVerifier((s1, s2) -> true)
								.build();
//		https://eodataservice.org/rasdaman/ows
		//		WebTarget webTarget = client.target("http://earthserver.ecmwf.int/rasdaman/ows");
		WebTarget webTarget = client.target("https://eodataservice.org/rasdaman/ows");
		String describeCoverageXML = webTarget
											 .queryParam("service", "WCS")
											 .queryParam("version", "2.0.1")
											 .queryParam("request", "DescribeCoverage")
											 .queryParam("coverageId", "L8_B10_32629_30") //ECMWF_SST_4326_05 //L8_B10_32629_30
											 .request().get(String.class);
		WCSResponse response = new WCSResponse();
		response.setResponse(describeCoverageXML);
		Pair<String, String> geoJson = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(describeCoverageXML);
		GeoRequests geoRequests = new GeoRequests("http://localhost:8083/femme-geo");
		DataElement dataElement = new DataElement();
		dataElement.setId("test");
		
		geoRequests.insert(GeoUtils.convertDataToCoverageGeo(response, "", dataElement));*/

	}
}
