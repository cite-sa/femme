package gr.cite.femme.geo.api;

import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.geo.CoverageGeo;

import java.io.IOException;
import java.util.List;

public interface GeoServiceApi {
    public List<CoverageGeo> getCoveragesByBboxString(String bBox) throws IOException, DatastoreException;

    public List<CoverageGeo> getCoveragesByPoint(Double longitude, Double latitude, Double radius) throws IOException, DatastoreException;
}
