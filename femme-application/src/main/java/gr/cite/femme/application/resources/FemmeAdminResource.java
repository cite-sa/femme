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

	private static final String COLLECTIONS_PATH = "collections";
	private static final String DATA_ELEMENTS_PATH = "dataElements";
	private static final String METADATA_PATH = "metadata";

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
	public Response insert(@NotNull Collection collection) throws FemmeApplicationException {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		
		try {
			if (StringUtils.isEmpty(collection.getName())) {
				collection.setName("collection_" + UUID.randomUUID().toString());
			} else {
				collection.setName(collection.getName().trim().toLowerCase().replaceAll(" ", "_"));
			}

			String collectionId = this.femme.insert(collection);

			location = this.uriInfo.getBaseUriBuilder().path(FemmeResource.class).path(FemmeAdminResource.COLLECTIONS_PATH).path(collectionId).build();

			String message = "Collection " + collectionId + " successfully inserted";
			logger.info(message);
			femmeResponse
					.setStatus(Response.Status.CREATED.getStatusCode())
					.setMessage(message)
					.setEntity(new FemmeResponseEntity<String>().setHref(location.toString()).setBody(collectionId));

		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			String errorMessage = "Collection insertion failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.CREATED).location(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(@NotNull DataElement dataElement) throws FemmeApplicationException {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();
		
		try {

			if (StringUtils.isEmpty(dataElement.getName())) {
				dataElement.setName("dataElement_" + UUID.randomUUID().toString());
			}

			//this.datastore.insert(dataElement);
			this.femme.insert(dataElement);
			location = this.uriInfo.getRequestUriBuilder().path(FemmeAdminResource.DATA_ELEMENTS_PATH).path(dataElement.getId()).build();
			entity.setHref(location.toString()).setBody(dataElement.getId());

			String message = "DataElement " + dataElement.getId() + " successfully inserted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.CREATED.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			String errorMessage = "DataElement insertion failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.CREATED).location(location).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@NotNull @PathParam("collectionId") String collectionId, @NotNull DataElement dataElement) throws FemmeApplicationException {
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
		} catch (FemmeException | DatastoreException | MetadataStoreException e) {
			String errorMessage = "DataElement insertion in Collection " + collectionId + " failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.CREATED).location(location).entity(femmeResponse).build();
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
		FemmeResponse<T> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<T> entity = new FemmeResponseEntity<>();

		element.setId(elementId);

		try {
			T updatedElement = this.femme.update(element);
			entity.setBody(updatedElement);

			String message = element.getClass().getSimpleName() + " " + element.getId() + " successfully updated";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (DatastoreException | MetadataStoreException e) {
			String errorMessage = element.getClass().getSimpleName() + " " + elementId + " update failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
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
			String metadatumId = this.femme.insert(metadatum);
			entity.setBody(metadatumId);

			String message = "Metadatum " + metadatumId + " successfully inserted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			String errorMessage = "Metadatum insertion failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}

	@POST
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCollectionMetadata(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull @PathParam("metadatumId") String metadatumId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return updateElementMetadata(collectionId, metadatumId, metadatum, Collection.class);
	}

	@POST
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataElementMetadata(
			@NotNull @PathParam("dataElementId") String dataElementId,
			@NotNull @PathParam("metadatumId") String metadatumId,
			@NotNull Metadatum metadatum) throws FemmeApplicationException {
		return updateElementMetadata(dataElementId, metadatumId, metadatum, DataElement.class);
	}

	private <T extends Element> Response updateElementMetadata(String elementId, String metadatumId, Metadatum metadatum, Class<T> elementSubType) throws FemmeApplicationException {
		FemmeResponse<Metadatum> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Metadatum> entity = new FemmeResponseEntity<>();

		metadatum.setId(metadatumId);
		metadatum.setElementId(elementId);

		try {
			Metadatum updatedMetadatum = this.femme.update(metadatum);
			//if (updatedMetadatum == null) {
			//	throw new FemmeApplicationException("No metadatum " + metadatumId + " found", Response.Status.NOT_FOUND.getStatusCode());
			//}
			entity.setBody(updatedMetadatum);

			String message = "Metadatum " + metadatumId + " successfully updated";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			String errorMessage = "Metadatum " + metadatumId + " of " + elementSubType.getSimpleName() + " " + elementId + " update failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.status(Response.Status.OK).entity(femmeResponse).build();
	}

	@DELETE
	@Path(FemmeAdminResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteCollectionMetadatum(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return deleteElementMetadata(collectionId, metadatumId);
	}

	@DELETE
	@Path(FemmeAdminResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeAdminResource.METADATA_PATH + "/{metadatumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDataElementMetadatum(
			@NotNull @PathParam("dataElementId") String dataElementId,
			@NotNull @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return deleteElementMetadata(dataElementId, metadatumId);
	}

	private Response deleteElementMetadata(String elementId, String metadatumId) throws FemmeApplicationException {
		FemmeResponse<Metadatum> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Metadatum> entity = new FemmeResponseEntity<>();

		try {
			this.femme.softDeleteMetadatum(metadatumId);

			String message = "Metadatum " + metadatumId + " successfully soft deleted";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity);
		} catch (FemmeException e) {
			String errorMessage = "Metadatum " + metadatumId + " of " + elementId + " soft deletion failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
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
			String errorMessage = "Reindexing failed";
			logger.error(errorMessage, e);
			throw new FemmeApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
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
			return Response.serverError().entity(femmeResponse).execute();
		}
		return Response.ok().entity(femmeResponse).execute();
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
			return Response.serverError().entity(femmeResponse).execute();
		}
		return Response.ok().entity(femmeResponse).execute();
	}*/
}
