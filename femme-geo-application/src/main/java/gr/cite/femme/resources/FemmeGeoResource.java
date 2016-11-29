package gr.cite.femme.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gr.cite.femme.client.FemmeClientException;
import gr.cite.femme.client.FemmeDatastoreException;
import gr.cite.femme.client.api.FemmeClientAPI;
import gr.cite.femme.client.query.CriterionBuilderClient;
import gr.cite.femme.client.query.QueryClient;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.QueryOptionsFields;

@Path("/")
public class FemmeGeoResource {
	
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
		
		QueryClient query = QueryClient.query().addCriterion(CriterionBuilderClient.root().eq("systemicMetadata.other.bbox.crs", crs).end());
		try {
			List<DataElement> dataElements = femmeClient.findDataElements(query, QueryOptionsFields.builder().include(new HashSet<>(Arrays.asList("collections"))).build(), null);
			for (DataElement dataElement: dataElements) {
				for(Collection collection: dataElement.getCollections()) {
					if (!collectionExistsInSet(collection, endpoints)) {						
						endpoints.add(collection);
					}
				}
			}
		} catch (FemmeDatastoreException | FemmeClientException e) {
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
	@Path("coverages/{endpointId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverages(@PathParam("endpointId") String endpointId) {
		
		List<DataElement> dataElements;
		
		/*QueryClient query = QueryClient.query()
				.addCriterion(CriterionBuilderClient.root().inAnyCollection(Arrays.asList(CriterionBuilderClient.root().eq("_id", endpointId).end())).end());*/
		
		try {
			dataElements = femmeClient.getDataElementsInCollectionById(endpointId);
		} catch (FemmeDatastoreException | FemmeClientException e) {
			throw new WebApplicationException(e.getMessage(), e);
		}
		
		return Response.ok(dataElements).build();
	}
}
