package gr.cite.earthserver.wcs.geo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import gr.cite.commons.utils.xml.XMLConverter;
import gr.cite.commons.utils.xml.XPathEvaluator;
import gr.cite.commons.utils.xml.exceptions.XMLConversionException;
import gr.cite.commons.utils.xml.exceptions.XPathEvaluationException;
import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.earthserver.wcs.utils.ParseException;
import gr.cite.earthserver.wcs.utils.WCSFemmeMapper;
import gr.cite.earthserver.wcs.utils.WCSParseUtils;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.model.BBox;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.utils.Pair;
import gr.cite.femme.core.geo.CoverageGeo;
import org.geojson.GeoJsonObject;
import org.geojson.Geometry;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.crypto.Data;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GeoUtils {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static final String DEFAULT_CRS = "EPSG:4326";

	public static Pair<String, String> getGeoJsonBoundingBoxFromDescribeCoverage(String describeCoverageXML) throws ParseException {
		String boundingBoxJSON;
		try {
			XPathEvaluator xPathEvaluator = new XPathEvaluator(XMLConverter.stringToNode(describeCoverageXML, true));
			List<Axis> axes = GeoUtils.getAxes(xPathEvaluator);
			String currentCrsString =axes.stream().filter(GeoUtils::isLongitude).map(Axis::getCrs).findFirst().orElseThrow(() -> new ParseException(""));
			CoordinateReferenceSystem defaultCrs = CRS.decode(GeoUtils.DEFAULT_CRS);
			CoordinateReferenceSystem currentCrs = CRS.decode(currentCrsString,true);
			ReferencedEnvelope envelope;
			try {
				System.out.println("1:"+axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException("")));
				envelope = new ReferencedEnvelope(
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLongitude(axes.stream().filter(GeoUtils::isLongitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getLowerCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						validateLatitude(axes.stream().filter(GeoUtils::isLatitude).map(Axis::getUpperCorner).findFirst().orElseThrow(() -> new ParseException(""))),
						currentCrs
				);
			} catch(MismatchedDimensionException e) {
				throw new ParseException(e);
			}
            System.out.println("lower:"+ envelope.getLowerCorner());
            System.out.println("upper:"+envelope.getUpperCorner());
            Envelope quick = envelope;
			if (!currentCrs.getName().equals(defaultCrs.getName())) {
			//	envelope = envelope.transform(defaultCrs, true,10);
				DirectPosition dpLc = envelope.getLowerCorner();
				DirectPosition dpUc = envelope.getUpperCorner();

				DirectPosition destLc = new DirectPosition2D();
				DirectPosition destUc = new DirectPosition2D();
                MathTransform transform = CRS.findMathTransform(currentCrs, defaultCrs,false);
				transform.transform(dpLc, destLc);
				transform.transform(dpUc, destUc);

                quick = JTS.transform(envelope, transform);
				//System.out.println("transformed:" + quick.getMinX() + "," + quick.getMinY());
				System.out.println( "transformed:"+((DirectPosition2D)destLc).x + " " + ((DirectPosition2D)destLc).y );

				//   Envelope better = JTS.transform(envelope, null, transform, 10);
			}
			Polygon geometry = JTS.toGeometry(quick);

			GeometryJSON geomJSON = new GeometryJSON();
			boundingBoxJSON = geomJSON.toString(geometry);

		} catch (XPathFactoryConfigurationException | XMLConversionException | XPathEvaluationException | MismatchedDimensionException | FactoryException | TransformException e) {
			throw new ParseException(e);
		}

		return new Pair<>(GeoUtils.DEFAULT_CRS, boundingBoxJSON);
	}

	private static Double validateLongitude( Double longitude){

		if( longitude < -180.0 ){
			return -180.0;
		}
		else if( longitude > 180.0){
			return 180.0;
		}
		return  longitude;
	}


	private static Double validateLatitude( Double longitude){

		if( longitude < -90.0 ){
			return -90.0;
		}
		else if( longitude > 90.0){
			return 90.0;
		}
		return  longitude;
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

	private static List<Axis> getAxes(XPathEvaluator evaluator) throws XPathEvaluationException {
		int crsDimension = GeoUtils.getCrsDimension(evaluator);
		List<String> axisLabels = GeoUtils.getAxisLabels(evaluator);
		List<String> crs = GeoUtils.getCrs(evaluator);
		List<String> lowerCorners = GeoUtils.getLowerCorners(evaluator);
		List<String> upperCorners = GeoUtils.getUpperCorners(evaluator);

		List<Axis> axes = new ArrayList<>();
		for (int axesIndex = 0; axesIndex < crsDimension; axesIndex++) {
			if (isLatitude(axisLabels.get(axesIndex)) || isLongitude(axisLabels.get(axesIndex))) {
				axes.add(new Axis(axisLabels.get(axesIndex), crs.get(0), lowerCorners.get(axesIndex), upperCorners.get(axesIndex)));
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
		return Arrays.stream(URI.create(crs.stream().findFirst().orElse("")).getQuery().split("&"))
				.map(queryParam -> queryParam.substring(queryParam.indexOf("/crs/") + 5, queryParam.indexOf("/0/")) + ":" + queryParam.substring(queryParam.lastIndexOf("/") + 1))
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


	public static CoverageGeo convertDataToCoverageGeo(WCSResponse coverage, DataElement dataElement){
		CoverageGeo coverageGeo = new CoverageGeo();
		try {
			Pair<String, String> geoJson = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(coverage.getResponse());
			GeoJsonObject object = mapper.readValue(geoJson.getRight(), GeoJsonObject.class);

			coverageGeo.setGeo(object);
			coverageGeo.setCoverageId(dataElement.getName());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return coverageGeo;
	}




	public static void main(String[] args) throws ParseException, KeyManagementException, NoSuchAlgorithmException {

		SSLContext sslcontext = SSLContext.getInstance("TLS");

		sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
		}}, new java.security.SecureRandom());

		Client client = ClientBuilder.newBuilder()
				.sslContext(sslcontext)
				.hostnameVerifier((s1, s2) -> true)
				.build();
//		https://eodataservice.org/rasdaman/ows
		//		WebTarget webTarget = client.target("http://earthserver.ecmwf.int/rasdaman/ows");
		WebTarget webTarget = client.target("http://earthserver.ecmwf.int/rasdaman/ows");
		String describeCoverageXML = webTarget
				.queryParam("service", "WCS")
				.queryParam("version", "2.0.1")
				.queryParam("request", "DescribeCoverage")
				.queryParam("coverageId", "ECMWF_SST_4326_05") //ECMWF_SST_4326_05 //L8_B10_32629_30
				.request().get(String.class);
		WCSResponse response = new WCSResponse();
		response.setResponse(describeCoverageXML);
		Pair<String, String> geoJson = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(describeCoverageXML);
		GeoRequests geoRequests = new GeoRequests();
		DataElement dataElement = new DataElement();
		dataElement.setId("test");
		try {
			geoRequests.insert(GeoUtils.convertDataToCoverageGeo(response,dataElement));
		} catch (FemmeException e) {
			e.printStackTrace();
		}

//		System.out.println(describeCoverageXML);
//		System.out.println(geoJson.getRight());
	}
}
