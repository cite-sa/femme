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
			ReferencedEnvelope envelope;
			
			if (currentCrs.equals(defaultCrs)) {
				envelope = new ReferencedEnvelope(
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						currentCrs
				);
			} else {
				envelope = new ReferencedEnvelope(
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						currentCrs
				);
			}
			
			Envelope quick = envelope;
			if (!currentCrs.getName().equals(defaultCrs.getName())) {
				//	envelope = envelope.transform(defaultCrs, true,10);
				DirectPosition dpLc = envelope.getLowerCorner();
				DirectPosition dpUc = envelope.getUpperCorner();
				
				DirectPosition destLc = new DirectPosition2D();
				DirectPosition destUc = new DirectPosition2D();
				MathTransform transform = CRS.findMathTransform(currentCrs, defaultCrs, false);
				transform.transform(dpLc, destLc);
				transform.transform(dpUc, destUc);
				
				quick = JTS.transform(envelope, transform);
				//System.out.println("transformed:" + quick.getMinX() + "," + quick.getMinY());
				//System.out.println("transformed:" + ((DirectPosition2D) destLc).x + " " + ((DirectPosition2D) destLc).y);
				
				//Envelope better = JTS.transform(envelope, null, transform, 10);
			}
			
			Polygon geometry = JTS.toGeometry(quick);
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
		String dc = "<wcs:CoverageDescriptions xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n" +
							"    xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\"\n" +
							"    xmlns:ows=\"http://www.opengis.net/ows/2.0\"\n" +
							"    xmlns:swe=\"http://www.opengis.net/swe/2.0\"\n" +
							"    xmlns:wcs=\"http://www.opengis.net/wcs/2.0\"\n" +
							"    xmlns:wcsgs=\"http://www.geoserver.org/wcsgs/2.0\"\n" +
							"    xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
							"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\" http://www.opengis.net/wcs/2.0 http://schemas.opengis.net/wcs/2.0/wcsDescribeCoverage.xsd\">\n" +
							"    <wcs:CoverageDescription gml:id=\"return_level2\"\n" +
							"        xmlns=\"http://www.opengis.net/gml/3.2\"\n" +
							"        xmlns:gmlcov=\"http://www.opengis.net/gmlcov/1.0\" xmlns:swe=\"http://www.opengis.net/swe/2.0\">\n" +
							"        <boundedBy>\n" +
							"            <Envelope axisLabels=\"Lat Long\" srsDimension=\"2\"\n" +
							"                srsName=\"http://localhost:8080/def/crs/EPSG/0/4326\" uomLabels=\"degree degree\">\n" +
							"                <lowerCorner>-90 -180</lowerCorner>\n" +
							"                <upperCorner>90 180</upperCorner>\n" +
							"            </Envelope>\n" +
							"        </boundedBy>\n" +
							"        <wcs:CoverageId>return_level2</wcs:CoverageId>\n" +
							"        <coverageFunction>\n" +
							"            <GridFunction>\n" +
							"                <sequenceRule axisOrder=\"+2 +1\">Linear</sequenceRule>\n" +
							"                <startPoint>0 0</startPoint>\n" +
							"            </GridFunction>\n" +
							"        </coverageFunction>\n" +
							"        <gmlcov:metadata/>\n" +
							"        <domainSet>\n" +
							"            <RectifiedGrid dimension=\"2\" gml:id=\"return_level2-grid\">\n" +
							"                <limits>\n" +
							"                    <GridEnvelope>\n" +
							"                        <low>0 0</low>\n" +
							"                        <high>1799 3599</high>\n" +
							"                    </GridEnvelope>\n" +
							"                </limits>\n" +
							"                <axisLabels>Lat Long</axisLabels>\n" +
							"                <origin>\n" +
							"                    <Point gml:id=\"return_level2-origin\" srsName=\"http://localhost:8080/def/crs/EPSG/0/4326\">\n" +
							"                        <pos>89.95 -179.95</pos>\n" +
							"                    </Point>\n" +
							"                </origin>\n" +
							"                <offsetVector srsName=\"http://localhost:8080/def/crs/EPSG/0/4326\">-0.1 0</offsetVector>\n" +
							"                <offsetVector srsName=\"http://localhost:8080/def/crs/EPSG/0/4326\">0 0.1</offsetVector>\n" +
							"            </RectifiedGrid>\n" +
							"        </domainSet>\n" +
							"        <gmlcov:rangeType>\n" +
							"            <swe:DataRecord>\n" +
							"                <swe:field name=\"field_1\">\n" +
							"                    <swe:Quantity xmlns:swe=\"http://www.opengis.net/swe/2.0\">\n" +
							"                        <swe:label>field_1</swe:label>\n" +
							"                        <swe:uom code=\"10^0\"/>\n" +
							"                    </swe:Quantity>\n" +
							"                </swe:field>\n" +
							"            </swe:DataRecord>\n" +
							"        </gmlcov:rangeType>\n" +
							"        <wcs:ServiceParameters>\n" +
							"            <wcs:CoverageSubtype>RectifiedGridCoverage</wcs:CoverageSubtype>\n" +
							"            <wcs:nativeFormat>application/octet-stream</wcs:nativeFormat>\n" +
							"        </wcs:ServiceParameters>\n" +
							"    </wcs:CoverageDescription>\n" +
							"</wcs:CoverageDescriptions>";
		Pair<String, String> bbox = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(dc);
		System.out.println(bbox);
		
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		
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
		
		geoRequests.insert(GeoUtils.convertDataToCoverageGeo(response, "", dataElement));


//		System.out.println(describeCoverageXML);
//		System.out.println(geoJson.getRight());
	}
}
