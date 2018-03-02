package gr.cite.femme.geo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gr.cite.femme.core.model.BBox;
import org.geojson.GeoJsonObject;
import org.geojson.GeoJsonObjectVisitor;
import org.geojson.Polygon;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;



public class GeoUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String DEFAULT_CRS = "EPSG:4326";


    public static GeoJsonObject getBBoxFromString(String bBoxInput) throws IOException, FactoryException {
        String[] bounds = bBoxInput.split(",");
        GeoJsonObject geoJson = null;
        if(bounds.length == 4){
            double[] bBounds = Arrays.stream(bounds)
                    .mapToDouble(Double::parseDouble)
                    .toArray();

            return  getGeoJsonFromBBoxInput(bBounds);
        }

        return geoJson;

    }

    public static GeoJsonObject getGeoJsonFromBBoxInput(double[] bBounds) throws FactoryException, IOException {
        CoordinateReferenceSystem defaultCrs = CRS.decode(GeoUtils.DEFAULT_CRS);
        //Revert bounds minX, minY, maxX, maxY to -> xMin,xMax,yMin,yMax

        ReferencedEnvelope envelope = new ReferencedEnvelope(bBounds[0],bBounds[2],bBounds[1],bBounds[3],
                defaultCrs
        );
        com.vividsolutions.jts.geom.Polygon geometry = JTS.toGeometry(envelope);

        GeometryJSON geomJSON = new GeometryJSON();
        String boundingBoxJSON = geomJSON.toString(geometry);
        GeoJsonObject geoJson = mapper.readValue(boundingBoxJSON, GeoJsonObject.class);
        return geoJson;
    }

    public static String buildGeoWithinQuery(String geoJson) throws IOException {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        ObjectNode geometry = JsonNodeFactory.instance.objectNode();
        ObjectNode parent = JsonNodeFactory.instance.objectNode();
        ObjectNode crs = JsonNodeFactory.instance.objectNode();
        ObjectNode property = JsonNodeFactory.instance.objectNode();


        ObjectNode geo= (ObjectNode) mapper.readTree(geoJson);

        crs.put("type","name");
        crs.set("properties",property.put("name","urn:x-mongodb:crs:strictwinding:EPSG:4326" ));
        geo.set("crs",crs);
        geometry.set("$geometry",geo);
        node.set("$geoWithin",geometry);
        parent.set("loc",node);
        return mapper.writeValueAsString(parent);
    }

//    public static GeoJsonObject getGeoJsonFromString(String bBoxInput) throws JsonProcessingException {
//        GeoJsonObject geoJson = new GeoJsonObject();
//        String[] bounds = bBoxInput.split(",");
//        if(bounds.length == 4){
//            String bBoxJson = mapper.writeValueAsString(bounds);
//        }
//
//        return bBox;
//
//    }


}
