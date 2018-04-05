package gr.cite.femme.geo.api;

import gr.cite.femme.core.geo.CoverageGeo;
import gr.cite.femme.geo.core.FemmeGeoException;

import java.util.List;

public interface GeoServiceApi {
    public List<CoverageGeo> getCoveragesByBboxString(String bBox) throws FemmeGeoException;

    public List<CoverageGeo> getCoveragesByPoint(Double longitude, Double latitude, Double radius) throws FemmeGeoException;
}
