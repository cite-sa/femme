package gr.cite.femme.application.resources;

import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.commons.utils.hash.HashGeneratorUtils;
import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.engine.Femme;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.dto.Import;
import gr.cite.femme.core.dto.ImportEndpoint;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.dto.FemmeResponseEntity;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.FemmeException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import jersey.repackaged.com.google.common.collect.Sets;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Path("importer")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeImportResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeImportResource.class);
	private static final String IMPORTS_PATH = "imports";

	@Context
	private UriInfo uriInfo;
	@Context
	private ResourceContext resourceContext;
	private Femme femme;
	private Map<String, Import> imports = new ConcurrentHashMap<>();

	@Inject
	public FemmeImportResource(Femme femme) {
		this.femme = femme;
	}

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("pong").build();
	}

	@GET
	@Path(FemmeImportResource.IMPORTS_PATH + "/{id}")
	public Response getImport(@PathParam("id") String id) {
		FemmeResponse<Import> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Import> entity = new FemmeResponseEntity<>();

		Import requestedImport = this.imports.get(id);
		entity.setBody(requestedImport);
		femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Import " + id + " found").setEntity(entity);

		return Response.ok(femmeResponse).build();
	}

	@POST
	@Path(FemmeImportResource.IMPORTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	synchronized public Response beginImport(@NotNull ImportEndpoint importEndpoint) throws FemmeApplicationException {
		URI location;
		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();

		Import newImport = new Import();
		try {
			newImport.setId(HashGeneratorUtils.generateMD5(importEndpoint.getEndpoint()));
		} catch (HashGenerationException e) {
			throw new FemmeApplicationException("Import initialization failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
		newImport.setEndpoint(importEndpoint.getEndpoint());

		if (this.imports.containsKey(newImport.getId())) {
			throw new FemmeApplicationException("Import " + newImport.getId() + " for endpoint " + newImport.getEndpoint() + " already in progress", Response.Status.BAD_REQUEST.getStatusCode());
		}
		this.imports.put(newImport.getId(), newImport);

		location = this.uriInfo.getRequestUriBuilder().path(FemmeImportResource.IMPORTS_PATH).path(newImport.getId()).build();
		entity.setHref(location.toASCIIString());
		entity.setBody(newImport.getId());
		femmeResponse.setStatus(Response.Status.CREATED.getStatusCode()).setMessage("New import" + newImport.getId() + " started").setEntity(entity);

		return Response.created(location).entity(femmeResponse).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path(FemmeImportResource.IMPORTS_PATH + "/{id}/collections")
	public Response importCollection(@PathParam("id") String id, Collection collection) throws FemmeApplicationException {
		if (!this.imports.containsKey(id)) {
			throw new FemmeApplicationException("No such import " + id + " currently in progress", Response.Status.NOT_FOUND.getStatusCode());
		}

		try {
			if (!id.equals(HashGeneratorUtils.generateMD5(collection.getEndpoint()))) {
				throw new FemmeApplicationException("Import id " + id + " and endpoint " + collection.getEndpoint() +  "do not match", Response.Status.BAD_REQUEST.getStatusCode());
			}
		} catch (HashGenerationException e) {
			throw new FemmeApplicationException("Collection import failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		Response response = this.resourceContext.getResource(FemmeAdminResource.class).insert(collection);
		Import existingImport = this.imports.get(id);
		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			existingImport.setCollectionId(collection.getId());
		}

		try {
			List<DataElement> existingDataElements = this.femme.query(DataElement.class).find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().inAnyCollection(
					Collections.singletonList(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collection
							.getId())).end()
					)).end())).options(QueryOptionsMessenger.builder().include(Sets.newHashSet("id")).build()).execute().list();
			existingImport.setExistingDataElements(existingDataElements.stream().map(DataElement::getId).collect(Collectors.toList()));
		} catch (DatastoreException | MetadataStoreException e) {
			throw new FemmeApplicationException("Collection import failed", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return response;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path(FemmeImportResource.IMPORTS_PATH + "/{id}/dataElements")
	public Response importInCollection(@PathParam("id") String id, DataElement dataElement) throws FemmeApplicationException {
		if (!this.imports.containsKey(id)) {
			throw new FemmeApplicationException("No such import " + id + " currently in progress", Response.Status.NOT_FOUND.getStatusCode());
		}

		if (dataElement.getSystemicMetadata() != null) {
			dataElement.getSystemicMetadata().setStatus(Status.PENDING);
		} else {
			dataElement.setSystemicMetadata(new SystemicMetadata());
			dataElement.getSystemicMetadata().setStatus(Status.PENDING);
		}

		Response response = this.resourceContext.getResource(FemmeAdminResource.class).addToCollection(this.imports.get(id).getCollectionId(), dataElement);
		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			this.imports.get(id).getNewDataElements().add(dataElement.getId());
		}
		return response;
	}

	@DELETE
	@Path(FemmeImportResource.IMPORTS_PATH + "/{id}")
	synchronized public Response endImport(@PathParam("id") String id) throws FemmeApplicationException {
		if (!this.imports.containsKey(id)) {
			throw new FemmeApplicationException("No such import " + id + " currently in progress", Response.Status.NOT_FOUND.getStatusCode());
		}

		FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		// TODO Make inactive old DataElments
		Import importToBeDeleted = this.imports.get(id);

		List<String> deactivate = importToBeDeleted.getExistingDataElements().stream()
				.filter(existingDataElement -> ! importToBeDeleted.getNewDataElements().contains(existingDataElement)).collect(Collectors.toList());
		for (String deactivateId: deactivate) {
			try {
				this.femme.deactivateElement(deactivateId, DataElement.class);
			} catch (FemmeException e) {
				logger.error(e.getMessage(), e);
			}
		}

		Import deletedImport = this.imports.remove(id);
		femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Import " + deletedImport.getId() + " ended successfully");

		return Response.ok(femmeResponse).build();
	}
}
