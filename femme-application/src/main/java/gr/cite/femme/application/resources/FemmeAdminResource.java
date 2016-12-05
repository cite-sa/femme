package gr.cite.femme.application.resources;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.dto.FemmeResponseEntity;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;

@Component
@Path("admin")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeAdminResource {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeAdminResource.class);
	
	@Context
	private UriInfo uriInfo;
	
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
	@Path("collections")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Collection collection) {
		String id = null;
		String location = null;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<String>();
		
		try {
			if (collection.getName() == null) {
				collection.setName(UUID.randomUUID().toString());
			}
			id = datastore.insert(collection);
			location = uriInfo.getBaseUri() + "collections/" + id;
			entity.setHref(location);
			entity.setBody(id);
			
			femmeResponse.setStatus(201).setMessage("Collection " + id + " successfully inserted").setEntity(entity);
			logger.info("Collection " + id + " successfully inserted");
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		
		return Response.created(URI.create(location)).entity(femmeResponse).build();
	}
	
	@POST
	@Path("dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(DataElement dataElement) {
		String id = null;
		String location = null;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<String>();
		
		try {
			id = datastore.insert(dataElement);
			location = uriInfo.getBaseUri() + "dataElements/" + id;
			entity.setHref(location);
			entity.setBody(id);
			
			femmeResponse.setStatus(201).setMessage("DataElement " + id + " successfully inserted").setEntity(entity);
			logger.info("DataElement " + id + " successfully inserted");
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}

		return Response.created(URI.create(location)).entity(femmeResponse).build();
	}

	@POST
	@Path("collections/{collectionId}/dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@PathParam("collectionId") String collectionId, DataElement dataElement) {
		String location = null;
		DataElement insertedDataElement = null;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<String>();
		
		try {
			insertedDataElement = datastore.addToCollection(dataElement, collectionId);
			location = uriInfo.getBaseUri() + "collections/" + collectionId + "/dataElements/" + insertedDataElement.getId();
			entity.setHref(location);
			entity.setBody(insertedDataElement.getId());
			
			femmeResponse.setStatus(201).setMessage("DataElement " + insertedDataElement.getId() + " successfully inserted in collection " + collectionId)
				.setEntity(entity);
			logger.info("DataElement " + insertedDataElement.getId() + " successfully inserted in Collection " + collectionId);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}

		return Response.created(URI.create(location)).entity(femmeResponse).build();
	}
	
}
