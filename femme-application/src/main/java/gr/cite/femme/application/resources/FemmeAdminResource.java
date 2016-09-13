package gr.cite.femme.application.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.mongodb.CriterionMongo;
import gr.cite.femme.query.mongodb.QueryMongo;

@Component
@Path("femme/admin")
public class FemmeAdminResource {
	
private static final Logger logger = LoggerFactory.getLogger(FemmeAdminResource.class);
	
	private Datastore<Criterion, Query<Criterion>> datastore;

	@Inject
	public FemmeAdminResource(Datastore<Criterion, Query<Criterion>> datastore) {
		this.datastore = datastore;
	}

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("pong").build();
	}

	@POST
	@Path("collections/collection")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<String> insert(Collection collection) {
		String insertedCollection = null;
		FemmeResponse<String> response = new FemmeResponse<>();
		
		try {
			insertedCollection = datastore.insert(collection);
			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(insertedCollection);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			response.setStatus(false);
			response.setMessage(e.getMessage());
		}
		
		return response;
	}
	
	@POST
	@Path("dataElements/dataElement")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<String> insert(DataElement dataElement) {
		String insertedDataElement = null;
		FemmeResponse<String> response = new FemmeResponse<>();
		
		try {
			insertedDataElement = datastore.insert(dataElement);
			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(insertedDataElement);
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;
	}

	@POST
	@Path("collections/{collectionId}/dataElements/dataElement")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<String> addToCollection(@PathParam("collectionId") String collectionId, DataElement dataElement) {
		DataElement insertedDataElement = null;
		FemmeResponse<String> response = new FemmeResponse<>();
		
		try {
			insertedDataElement = datastore.addToCollection(dataElement, collectionId);
			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(insertedDataElement.getId());
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;
	}
}
