package gr.cite.femme.geo.resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.core.model.BBox;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.dto.QueryOptionsMessenger;

@Path("/")
public class FemmeGeoResource {
	private static final ObjectMapper mapper = new ObjectMapper();
	private FemmeClientAPI femmeClient;
	
	@Inject
	public FemmeGeoResource(FemmeClientAPI femmeClient) {
		this.femmeClient = femmeClient;
	}
	
	@GET
	@Path("endpoints/{crs}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpointsByCRS(@PathParam("crs") String crs) {
		Set<Collection> endpoints = new HashSet<>();
		
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("systemicMetadata.geo.bbox.crs", crs).end());
		try {
			List<DataElement> dataElements = femmeClient.findDataElements(query, QueryOptionsMessenger.builder().include(new HashSet<>(Collections.singletonList("collections"))).build(), null);
			for (DataElement dataElement: dataElements) {
				for(Collection collection: dataElement.getCollections()) {
					if (!collectionExistsInSet(collection, endpoints)) {						
						endpoints.add(collection);
					}
				}
			}
		} catch (FemmeException | FemmeClientException e) {
			throw new WebApplicationException(e.getMessage(), e);
		}
		return Response.ok(endpoints).build();
	}
	
	private boolean collectionExistsInSet(Collection collection, Set<Collection> collections) {
		
		for (Collection collectionFromSet : collections) {
			if (collectionFromSet.getId().equals(collection.getId())) {
				return true;
			}
		}
		return false;
	}
	
	@GET
	@Path("endpoints/{endpointId}/coverages")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverages(@PathParam("endpointId") String endpointId) {
		
		List<DataElement> dataElements;
		
		/*QueryClient query = QueryClient.query()
				.addCriterion(CriterionBuilderClient.root().inAnyCollection(Arrays.asList(CriterionBuilderClient.root().eq("_id", endpointId).end())).end());*/
		
		try {
			dataElements = femmeClient.getDataElementsInCollectionById(endpointId);
		} catch (FemmeException | FemmeClientException e) {
			throw new WebApplicationException(e.getMessage(), e);
		}
		
		return Response.ok(dataElements).build();
	}
	
	@GET
	@Path("coverages/{dataElementId}/bbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBBox(@PathParam("dataElementId") String dataElementId) throws JsonParseException, JsonMappingException, IOException {
		
		
		Object geoJson = null;
		/*QueryClient query = QueryClient.query()
				.addCriterion(CriterionBuilderClient.root().inAnyCollection(Arrays.asList(CriterionBuilderClient.root().eq("_id", endpointId).end())).end());*/
		try {
			DataElement dataElement = femmeClient.getDataElementById(dataElementId);
			Map<String, String> geo = dataElement.getSystemicMetadata().getGeo();
			if (geo != null) {
				String bboxJson = geo.get("bbox");
				BBox bbox = mapper.readValue(bboxJson, BBox.class);
				if (bbox != null) {
					geoJson = bbox.getGeoJson();
				}
			}
			
		} catch (FemmeException e) {
			throw new WebApplicationException(e.getMessage(), e);
		}

		
		return Response.ok(geoJson).build();
	}
}
