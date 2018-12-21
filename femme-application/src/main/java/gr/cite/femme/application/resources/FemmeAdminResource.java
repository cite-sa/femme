package gr.cite.femme.application.resources;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.engine.Femme;
import gr.cite.femme.core.exceptions.FemmeException;
import org.apache.commons.lang3.StringUtils;
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
	
	private String rootUrl;

	private static final String COLLECTIONS_PATH = "collections";
	private static final String DATA_ELEMENTS_PATH = "dataElements";
	private static final String METADATA_PATH = "metadata";
	
	private Femme femme;

	@Inject
	public FemmeAdminResource(String rootUrl, Femme femme) {
		this.rootUrl = rootUrl;
		this.femme = femme;
	}

	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertCollection(@NotNull Collection collection) throws FemmeApplicationException {
		if ("".equals(collection.getName().trim())) {
			collection.setName("collection_" + UUID.randomUUID().toString());
		} else {
			collection.setName(collection.getName().trim().toLowerCase().replaceAll(" ", "_"));
		}
		
		return insert(collection);
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertDataElement(@NotNull DataElement dataElement) throws FemmeApplicationException {
		if ("".equals(dataElement.getName())) {
			dataElement.setName(UUID.randomUUID().toString());
		}
		
		return insert(dataElement);
	}

	private <T extends Element> Response insert(T element) throws FemmeApplicationException {
		FemmeResponse<String> femmeResponse;

		try {
			String elementId = this.femme.upsert(element);
			
			String message = element.getClass().getSimpleName() + " " + element.getId() + " successfully inserted";
			int statusCode = elementId != null ? Response.Status.CREATED.getStatusCode() : Response.Status.OK.getStatusCode();
			
			femmeResponse = buildResponse(element.getClass(), element.getId(), element.getId(), statusCode, message);
		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			throw buildFemmeApplicationExceptionForError(element.getClass().getSimpleName() + " insertion failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(femmeResponse.getStatus()).location(URI.create(femmeResponse.getEntity().getHref())).entity(femmeResponse).build();
	}

	// TODO OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@NotNull @PathParam("collectionId") String collectionId, @NotNull DataElement dataElement) throws FemmeApplicationException {
		FemmeResponse<String> femmeResponse;
		
		try {
			String dataElementId = this.femme.addToCollection(dataElement, collectionId);

			String message = "DataElement " + dataElement.getId() + " successfully " + (dataElementId != null ? "inserted" : "updated") +  " in collection " + collectionId;
			int statusCode = dataElementId != null ? Response.Status.CREATED.getStatusCode() : Response.Status.OK.getStatusCode();
			
			femmeResponse = buildResponse(DataElement.class, dataElement.getId(), dataElement.getId(), statusCode, message);
			
		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			throw buildFemmeApplicationExceptionForError("DataElement insertion in Collection " + collectionId + " failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(femmeResponse.getStatus()).location(URI.create(femmeResponse.getEntity().getHref())).entity(femmeResponse).build();
	}
	
	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCollection(@NotNull @PathParam("id") String id, @NotNull Collection collection) throws FemmeApplicationException {
		return updateElement(id, collection);
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataElement(@NotNull @PathParam("id") String id, @NotNull DataElement dataElement) throws FemmeApplicationException {
		return updateElement(id, dataElement);
	}

	private <T extends Element> Response updateElement(String elementId, T element) throws FemmeApplicationException {
		FemmeResponse<T> femmeResponse;

		element.setId(elementId);

		try {
			T updatedElement = this.femme.updateElement(element);

			String message = element.getClass().getSimpleName() + " " + element.getId() + " successfully updated";
			
			femmeResponse = buildResponse(element.getClass(), updatedElement.getId(), updatedElement, Response.Status.OK.getStatusCode(), message);
		} catch (DatastoreException | MetadataStoreException e) {
			throw buildFemmeApplicationExceptionForError(element.getClass().getSimpleName() + " " + elementId + " update failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.METADATA_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertCollectionMetadatum(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return insertMetadatum(metadatum, collectionId);
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeAdminResource.METADATA_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertDataElementMetadata(
			@NotNull @PathParam("dataElementId") String dataElementId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return insertMetadatum(metadatum, dataElementId);
	}

	private Response insertMetadatum(Metadatum metadatum, String elementId) throws FemmeApplicationException {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();

		metadatum.setElementId(elementId);

		try {
			String metadatumId = this.femme.insertMetadatum(metadatum);
			entity.setBody(metadatumId);

			String message = "Metadatum " + metadatumId + " successfully inserted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (MetadataStoreException e) {
			throw buildFemmeApplicationExceptionForError("Metadatum insertion failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
		
		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCollectionMetadatum(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull @PathParam("metadatumId") String metadatumId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return updateElementMetadatum(collectionId, metadatumId, metadatum, Collection.class);
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataElementMetadatum(
			@NotNull @PathParam("dataElementId") String dataElementId,
			@NotNull @PathParam("metadatumId") String metadatumId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return updateElementMetadatum(dataElementId, metadatumId, metadatum, DataElement.class);
	}

	private Response updateElementMetadatum(String elementId, String metadatumId, Metadatum metadatum, Class<? extends Element> elementSubType) throws FemmeApplicationException {
		FemmeResponse<Metadatum> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Metadatum> entity = new FemmeResponseEntity<>();

		metadatum.setId(metadatumId);
		metadatum.setElementId(elementId);

		try {
			Metadatum updatedMetadatum = this.femme.updateMetadatum(metadatum);
			//if (updatedMetadatum == null) {
			//	throw new FemmeApplicationException("No metadatum " + metadatumId + " found", Response.Status.NOT_FOUND.getStatusCode());
			//}
			entity.setBody(updatedMetadatum);

			String message = "Metadatum " + metadatumId + " successfully updated";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			throw buildFemmeApplicationExceptionForError("Metadatum " + metadatumId + " of " + elementSubType.getSimpleName() + " " + elementId + " update failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}

	@POST
	@Path("index")
	public Response reIndex() throws FemmeApplicationException {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();

		try {
			this.femme.reIndex();

			String message = "Reindexing successfully completed";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message);
		} catch (MetadataStoreException | MetadataIndexException e) {
			throw buildFemmeApplicationExceptionForError("Reindexing failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@DELETE
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}")
	public Response deleteCollection(@PathParam("collectionId") String collectionId) throws FemmeApplicationException {
		return deleteElement(collectionId, Collection.class);
	}

	@DELETE
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}")
	public Response deleteDataElement(@PathParam("dataElementId") String dataElementId) throws FemmeApplicationException {
		return deleteElement(dataElementId, DataElement.class);
	}

	private Response deleteElement(String elementId, Class<? extends Element> elementSubtype) throws FemmeApplicationException {
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();

		try {
			if (!this.femme.exists(elementSubtype, elementId)) {
				throw buildFemmeApplicationExceptionForInfo(elementSubtype.getSimpleName() + " [" + elementId + "] doesn't exist", Response.Status.NOT_FOUND.getStatusCode());
			}

			this.femme.deleteElement(elementId, elementSubtype);
			
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(elementSubtype.getSimpleName() + " " + elementId + " successfully deleted");
			logger.info(femmeResponse.getMessage());
			
			return Response.ok().entity(femmeResponse).build();
		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			throw buildFemmeApplicationExceptionForError(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
	}
	
	@DELETE
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteCollectionMetadatum(
		@NotNull @PathParam("collectionId") String collectionId,
		@NotNull @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return deleteElementMetadatum(collectionId, metadatumId);
	}
	
	@DELETE
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDataElementMetadatum(
		@NotNull @PathParam("dataElementId") String dataElementId,
		@NotNull @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return deleteElementMetadatum(dataElementId, metadatumId);
	}
	
	private Response deleteElementMetadatum(String elementId, String metadatumId) throws FemmeApplicationException {
		FemmeResponse<Metadatum> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Metadatum> entity = new FemmeResponseEntity<>();
		
		try {
			this.femme.softDeleteMetadatum(metadatumId);
			
			String message = "Metadatum " + metadatumId + " successfully soft deleted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			throw buildFemmeApplicationExceptionForError("Metadatum " + metadatumId + " of " + elementId + " soft deletion failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
		
		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}
	
	private <U, V> FemmeResponse<V> buildResponse(Class<U> elementSubtype, String elementId, V body, int statusCode, String message) {
		FemmeResponse<V> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<V> entity = new FemmeResponseEntity<>();
		
		String elementPath = elementSubtype.getSimpleName().substring(0, 1).toLowerCase() + elementSubtype.getSimpleName().substring(1) + "s";
		URI location = UriBuilder.fromUri(this.rootUrl).path(elementPath).path(elementId).build();
		entity.setHref(location.toString()).setBody(body);
		entity.setBody(body);
		
		femmeResponse.setStatus(statusCode).setMessage(message).setEntity(entity);
		
		logger.info(message);
		
		return femmeResponse;
	}
	
	private FemmeApplicationException buildFemmeApplicationExceptionForInfo(String message, int status) {
		logger.info(message);
		return new FemmeApplicationException(message, status);
	}
	
	private FemmeApplicationException buildFemmeApplicationExceptionForError(String message, int status, Exception exception) {
		logger.error(message, exception);
		return new FemmeApplicationException(message, status, exception);
	}
}
