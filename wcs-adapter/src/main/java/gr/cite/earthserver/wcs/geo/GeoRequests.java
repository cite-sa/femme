package gr.cite.earthserver.wcs.geo;

import gr.cite.femme.client.FemmeClient;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.geo.core.CoverageGeo;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GeoRequests {
    private static final Logger logger = LoggerFactory.getLogger(GeoRequests.class);

    private WebTarget webTarget;
    private Client client;


    private static final String GEO_URL = "http://localhost:8084/femme-geo-application-devel";

    public GeoRequests(){
        client = ClientBuilder.newClient().register(JacksonFeature.class);
        webTarget = client.target(GEO_URL);
    }

    public String insert(CoverageGeo coverageGeo) throws FemmeException {

        Response response = webTarget.path("admin/coverages")
                .request().post(Entity.entity(coverageGeo, MediaType.APPLICATION_JSON));

        FemmeResponse<String> femmeResponse = response.readEntity(new GenericType<FemmeResponse<String>>(){});
        if (response.getStatus() != 200) {
            logger.error(femmeResponse.getMessage());
            throw new FemmeException(femmeResponse.getMessage());
        }
        logger.debug("CoverageGeo " + femmeResponse.getEntity().getBody() + " has been successfully inserted");

        return femmeResponse.getEntity().getBody();
    }

}
