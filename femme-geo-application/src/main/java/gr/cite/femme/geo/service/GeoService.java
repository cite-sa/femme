package gr.cite.femme.geo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.geo.api.GeoServiceApi;
import gr.cite.femme.geo.core.CoverageGeo;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import gr.cite.femme.geo.utils.GeoUtils;
import org.geojson.GeoJsonObject;
import org.opengis.referencing.FactoryException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

public class GeoService implements GeoServiceApi{

    private MongoGeoDatastore mongoGeoDatastore;
    private static final ObjectMapper mapper = new ObjectMapper();


    public GeoService(){
        this.mongoGeoDatastore = new MongoGeoDatastore();
    }

    @Override
    public CoverageGeo getCoverageByBboxString(String bBox) throws JsonProcessingException, DatastoreException {
        GeoJsonObject geoJsonObject = null;
        try {
            geoJsonObject = GeoUtils.getBBoxFromString(bBox);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        CoverageGeo coverageGeo = mongoGeoDatastore.getCoverageByPolygon(geoJsonObject);
        String json= mapper.writeValueAsString(geoJsonObject);
        System.out.println("geoObject:"+json);
        return coverageGeo;
    }
}
