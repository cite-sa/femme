package gr.cite.femme.geo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.geo.api.GeoServiceApi;
import gr.cite.femme.geo.engine.mongodb.MongoGeoDatastore;
import gr.cite.femme.geo.utils.GeoUtils;
import org.geojson.GeoJsonObject;
import org.opengis.referencing.FactoryException;

import java.io.IOException;
import java.util.List;

public class GeoService implements GeoServiceApi{

    private MongoGeoDatastore mongoGeoDatastore;
    private static final ObjectMapper mapper = new ObjectMapper();


    public GeoService(){
        this.mongoGeoDatastore = new MongoGeoDatastore();
    }

    @Override
    public List<CoverageGeo> getCoveragesByBboxString(String bBox) throws IOException, DatastoreException {
        GeoJsonObject geoJsonObject = null;
        try {
            geoJsonObject = GeoUtils.getBBoxFromString(bBox);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return  mongoGeoDatastore.getCoveragesByPolygon(geoJsonObject);

    }

    @Override
    public List<CoverageGeo> getCoveragesByPoint(Double longitude, Double latitude, Double radius) throws IOException, DatastoreException {
        return mongoGeoDatastore.getCoverageByCoords(longitude, latitude, radius);

    }


}
