package gr.cite.femme.application.resources;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

import gr.cite.femme.engine.Femme;
import gr.cite.femme.core.exceptions.FemmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.dto.FemmeResponseEntity;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;

@Component
@Path("admin")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeAdminResource {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeAdminResource.class);
	private static final String COLLECTIONS_PATH = "collections";
	private static final String DATA_ELEMENTS_PATH = "dataElements";

	@Context
	private UriInfo uriInfo;
	private Femme femme;

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
	public Response insert(@NotNull Collection collection) {
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
			entity.setHref(location.toString()).setBody(collection.getId());

			String message = "Collection " + collection.getId() + " successfully inserted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.CREATED.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}

		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(@NotNull DataElement dataElement) {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {
			//this.datastore.insert(dataElement);
			this.femme.insert(dataElement);
			location = this.uriInfo.getRequestUriBuilder().path(FemmeAdminResource.DATA_ELEMENTS_PATH).path(dataElement.getId()).build();
			entity.setHref(location.toString()).setBody(dataElement.getId());

			String message = "DataElement " + dataElement.getId() + " successfully inserted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.CREATED.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).setMessage(e.getMessage());
			return Response.serverError().entity(femmeResponse).build();
		}

		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@NotNull @PathParam("collectionId") String collectionId, @NotNull DataElement dataElement) {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {
			//this.datastore.addToCollection(dataElement, collectionId);
			this.femme.addToCollection(dataElement, collectionId);

			location = this.uriInfo.getRequestUriBuilder().path(FemmeAdminResource.DATA_ELEMENTS_PATH).path(dataElement.getId()).build();
			entity.setHref(location.toString()).setBody(dataElement.getId());

			String message = "DataElement " + dataElement.getId() + " successfully inserted in collection " + collectionId;
			logger.info(message);
			femmeResponse.setStatus(Response.Status.CREATED.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).setMessage(e.getMessage());
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

			String message = "Reindexing successfully completed";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message);
		} catch (FemmeException e) {
			logger.error(e.getMessage(), e);
			femmeResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).setMessage(e.getMessage());
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
