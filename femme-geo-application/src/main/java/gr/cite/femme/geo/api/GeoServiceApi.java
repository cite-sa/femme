package gr.cite.femme.geo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.geo.core.CoverageGeo;

public interface GeoServiceApi {
    public CoverageGeo getCoverageByBboxString(String bBox) throws JsonProcessingException, DatastoreException;
}
