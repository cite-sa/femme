package gr.cite.femme.application.resources;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import gr.cite.femme.Femme;
import gr.cite.femme.exceptions.FemmeException;
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
	private static final String COLLECTIONS_PATH = "collections";
	private static final String DATA_ELEMENTS_PATH = "dataElements";

	@Context
	private UriInfo uriInfo;

	//private Datastore datastore;
	private Femme femme;

	/*@Inject
	public FemmeAdminResource(Datastore datastore) {
		this.datastore = datastore;
	}*/

	@Inject
	public FemmeAdminResource(Femme femme) {
		this.femme = femme;
	}

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Collection collection) {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {
			if (collection.getName() == null) {
				collection.setName(UUID.randomUUID().toString());
			}
			//this.datastore.insert(collection);
			this.femme.insert(collection);
			location = this.uriInfo.getBaseUriBuilder().path(FemmeResource.class).path(FemmeAdminResource.COLLECTIONS_PATH).path(collection.getId()).build();
			entity.setHref(location.toString());
			entity.setBody(collection.getId());
			
			femmeResponse.setStatus(201).setMessage("Collection " + collection.getId() + " successfully inserted").setEntity(entity);
			logger.info("Collection " + collection.getId() + " successfully inserted");
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(DataElement dataElement) {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {
			//this.datastore.insert(dataElement);
			this.femme.insert(dataElement);
			location = this.uriInfo.getRequestUriBuilder().path(FemmeAdminResource.DATA_ELEMENTS_PATH).path(dataElement.getId()).build();
			entity.setHref(location.toString());
			entity.setBody(dataElement.getId());
			
			femmeResponse.setStatus(201).setMessage("DataElement " + dataElement.getId() + " successfully inserted").setEntity(entity);
			logger.info("DataElement " + dataElement.getId() + " successfully inserted");
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@PathParam("collectionId") String collectionId, DataElement dataElement) {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {
			//this.datastore.addToCollection(dataElement, collectionId);
			this.femme.addToCollection(dataElement, collectionId);
			location = this.uriInfo.getRequestUriBuilder().path(FemmeAdminResource.DATA_ELEMENTS_PATH).path(dataElement.getId()).build();
			entity.setHref(location.toString());
			entity.setBody(dataElement.getId());
			
			femmeResponse.setStatus(201)
					.setMessage("DataElement " + dataElement.getId() + " successfully inserted in collection " + collectionId)
					.setEntity(entity);
			logger.info("DataElement " + dataElement.getId() + " successfully inserted in Collection " + collectionId);
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Path("index")
	public Response reIndex() {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		try {
			this.femme.reIndex();
			femmeResponse.setStatus(200).setMessage("Reindexing successfully completed");
			logger.info("Reindexing successfully completed");
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.ok().entity(femmeResponse).build();
	}

	/*@DELETE
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}")
	public Response deleteCollection(@PathParam("collectionId") String collectionId) {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();

		try {
			this.datastore.delete(collectionId, Collection.class);

			femmeResponse.setStatus(200).setMessage("Collection " + collectionId + " successfully deleted");
			logger.info("Collection " + collectionId + " successfully deleted");
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.ok().entity(femmeResponse).build();
	}

	@DELETE
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}")
	public Response deleteDataElement(@PathParam("dataElementId") String dataElementId) {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();

		try {
			this.datastore.delete(dataElementId, DataElement.class);

			femmeResponse.setStatus(200).setMessage("DataElement " + dataElementId + " successfully deleted");
			logger.info("Collection " + dataElementId + " successfully deleted");
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(500).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}
		return Response.ok().entity(femmeResponse).build();
	}*/
}
