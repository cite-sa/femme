package gr.cite.femme.application.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import gr.cite.commons.utils.xml.XMLFormatter;
import gr.cite.femme.core.dto.ElementList;
import gr.cite.femme.core.dto.MetadataList;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.engine.Femme;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.dto.DataElementList;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.dto.FemmeResponseEntity;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;
import gr.cite.femme.engine.query.construction.mongodb.CriterionMongo;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;

@Component
@Path("")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class FemmeResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);

	private static final String COLLECTIONS_PATH = "collections";
	private static final String DATA_ELEMENTS_PATH = "dataElements";
	private static final String METADATA_PATH = "metadata";
	
	@Context
	private UriInfo uriInfo;
	private Femme femme;

	@Inject
	public FemmeResource(Femme femme) {
		this.femme = femme;
	}
	
	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findCollections(
			@QueryParam("query") QueryMongo query,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath,
			@QueryParam("pretty") boolean pretty) throws FemmeApplicationException {

		return findElements(Collection.class, query, options, xPath, pretty);
	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{id}")
	public Response getCollectionById(
			@NotNull @PathParam("id") String id,
			@QueryParam("xpath") String xPath,
			@QueryParam("pretty") boolean pretty,
			@DefaultValue("false") @QueryParam("loadInactiveMetadata") boolean loadInactiveMetadata) throws FemmeApplicationException {

		return getElementById(Collection.class, id, xPath, pretty, loadInactiveMetadata);
	}

	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/name/{name}")
	public Response getCollectionByName(@NotNull @PathParam("name") String name) throws FemmeApplicationException {
		FemmeResponse<Collection> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Collection> entity = new FemmeResponseEntity<>();

		try {
			Collection collection = this.femme.query(Collection.class).find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.NAME, name).end())).execute().first();
			if (collection == null) {
				throw new FemmeApplicationException("No collection with name " + name + " found", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Collection " + name + " found").setEntity(entity);
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response findDataElements(
			@QueryParam("query") QueryMongo query,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath,
			@QueryParam("pretty") boolean pretty) throws FemmeApplicationException {

		return findElements(DataElement.class, query, options, xPath, pretty);
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/xpath")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response xPathInMemoryDataElements(
			@QueryParam("query") QueryMongo query,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {

		if (query == null) {
			logger.info("Query all DataElements");
		} else {
			logger.info("Query DataElements: " + query.build());
		}

		DataElementList dataElementList;
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			List<DataElement> dataElements = this.femme.query(DataElement.class).find(query).xPathInMemory(xPath).options(options).execute().list();
			dataElementList = new DataElementList(dataElements);
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		String message;
		if (dataElementList.getSize() == 0) {
			message = "No data elements found";
			logger.info(message);
			throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
		} else {
			message = dataElementList.getSize() + " data elements found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message)
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElementList));
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/{id}")
	public Response getDataElementById(
			@NotNull @PathParam("id") String id,
			@QueryParam("xpath") String xPath,
			@QueryParam("pretty") boolean pretty,
			@DefaultValue("false") @QueryParam("loadInactiveMetadata") boolean loadInactiveMetadata) throws FemmeApplicationException {

		return getElementById(DataElement.class, id, xPath, pretty, loadInactiveMetadata);
	}

	private Response findElements(Class<? extends Element> elementType, QueryMongo query, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {

		if (query == null) {
			logger.debug("Query all " + elementType.getSimpleName());
		} else {
			logger.debug("Query " +  elementType.getSimpleName() + " : " + query.build());
		}

		if (options != null && options.getLimit() != null && options.getLimit() == 0) {
			return countQuery(elementType, query, xPath);
		} else {
			return findQuery(elementType, query, options, xPath, pretty);
		}
	}

	private Response countQuery(Class<? extends Element> elementType, QueryMongo query, String xPath) throws FemmeApplicationException {
		logger.info("Count query");
		FemmeResponse<Long> femmeResponse = new FemmeResponse<>();
		Long totalElements;

		try {
			totalElements = this.femme.query(elementType).count(query).xPath(xPath).execute();
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		String message = totalElements + " " + elementType.getSimpleName() + " found";
		femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(new FemmeResponseEntity<>(totalElements));
		logger.info(message);

		return Response.ok().entity(femmeResponse).build();
	}

	private Response findQuery(Class<? extends Element> elementType, QueryMongo query, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {
		FemmeResponse<ElementList<? extends Element>> femmeResponse = new FemmeResponse<>();
		List<? extends Element> elements;
		try {
			elements = this.femme.query(elementType).find(query).options(options).xPath(xPath).execute().list();

			if (pretty) {
				elements.forEach(element -> FemmeResource.prettifyMetadata(element.getMetadata()));
			}
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		}

		String message;
		if (elements.isEmpty()) {
			message = "No " + elementType.getSimpleName() + "found";
			logger.info(message);
			throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
		} else {
			message = elements.size() + " " + elementType.getSimpleName() + " found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(new FemmeResponseEntity<>(new ElementList<>(elements)));
		}

		return Response.ok().entity(femmeResponse).build();
	}

	private Response getElementById(Class<? extends Element> elementType, String id, String xPath, boolean pretty, boolean loadInactiveMetadata) throws FemmeApplicationException {
		FemmeResponse<Element> femmeResponse = new FemmeResponse<>();

		try {
			Element element = xPath == null ? this.femme.get(id, elementType, loadInactiveMetadata) : this.femme.get(id, xPath, elementType);
			if (pretty) {
				FemmeResource.prettifyMetadata(element.getMetadata());
			}

			String message;
			if (element == null) {
				message = xPath == null || xPath.trim().isEmpty()
						? "No " + elementType.getSimpleName() + " with id " + id + " found"
						: "No " + elementType.getSimpleName() + " with id " + id + " and XPath " + xPath + " found";
				logger.info(message);
				throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
			}

			message = xPath == null || xPath.trim().isEmpty()
					? elementType.getSimpleName() + " " + element.getId() + " found"
					: elementType.getSimpleName() + " " +  element.getId() + " and XPath " + xPath + " found";
			logger.info(message);

			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(new FemmeResponseEntity<>(element));
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response findDataElementsByIds(
			@QueryParam("id") List<String> ids,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		
		QueryMongo query = null;
		if (ids.size() > 0) {
			List<CriterionMongo> idsCriteria = ids.stream().map(id -> CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()).collect(Collectors.toList());
			query = QueryMongo.query().addCriterion(CriterionBuilderMongo.root().or(idsCriteria).end());
		}
		if (query == null) {
			logger.info("Query all DataElements");
		} else {
			logger.info("Query on DataElements: " + query.build());
		}
		
		
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			List<DataElement> dataElements = this.femme.query(DataElement.class).find(query).xPath(xPath).options(options).execute().list();
			DataElementList dataElementList = new DataElementList(dataElements);
			
			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				femmeResponse.setStatus(Response.Status.OK.getStatusCode())
					.setMessage(dataElementList.getSize() + " data elements found")
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElementList));
			}

		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		
		return Response.ok().entity(femmeResponse).build();

	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.DATA_ELEMENTS_PATH)
	public Response getDataElementsInCollection(
			@NotNull @PathParam("collectionId") String collectionId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		return getDataElementsInCollectionWithFieldValue(FieldNames.ID, collectionId, options, xPath);
	}

	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{field}/{value}/" + FemmeResource.DATA_ELEMENTS_PATH)
	public Response getDataElementsInCollectionWithField(
			@PathParam("field") String field,
			@PathParam("value") String value,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {

		if (!FieldNames.NAME.equals(field)) {
			throw new FemmeApplicationException("Unsupported field " + field, Response.Status.BAD_REQUEST.getStatusCode());
		}
		return getDataElementsInCollectionWithFieldValue(field, value, options, xPath);
	}

	private Response getDataElementsInCollectionWithFieldValue(String field, String value, QueryOptionsMessenger options, String xPath) throws FemmeApplicationException {
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().inAnyCollection(Collections.singletonList(
							CriterionBuilderMongo.root().eq(field, FieldNames.ID.equals(field) ? new ObjectId(value) : value).end()
					)).end());

			List<DataElement> dataElements = this.femme.query(DataElement.class).find(query).xPath(xPath).options(options).execute().list();
			DataElementList dataElementList = new DataElementList(dataElements);

			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode())
					.setMessage(dataElementList.getSize() + " data elements found")
					.setEntity(new FemmeResponseEntity<>(this.uriInfo.getBaseUriBuilder().path(this.uriInfo.getPath()).toString(), dataElementList));

		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.DATA_ELEMENTS_PATH  + "/{dataElementId}")
	public Response getDataElementsInCollection(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull @PathParam("dataElementId") String dataElementId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {

		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			CriterionMongo collectionCriterion = CriterionBuilderMongo.root().inAnyCollection(Collections.singletonList(
					CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end())).end();

			CriterionMongo dataElementCriterion = CriterionBuilderMongo.root().eq(FieldNames.ID, dataElementId).end();

			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());

			List<DataElement> dataElements = this.femme.query(DataElement.class).find(query).options(options).execute().list();
			DataElementList dataElementList = new DataElementList(dataElements);
			
			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode())
				.setMessage(dataElementList.getSize() + " data elements found")
				.setEntity(new FemmeResponseEntity<>(this.uriInfo.getBaseUriBuilder().path(this.uriInfo.getPath()).toString(), dataElementList));
			
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{id}/" + FemmeResource.METADATA_PATH)
	public Response getCollectionMetadata(@PathParam("id") String id) throws FemmeApplicationException {
		return getElementMetadata(id, Collection.class);
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/{id}/" + FemmeResource.METADATA_PATH)
	public Response getDataElementMetadata(@PathParam("id") String id) throws FemmeApplicationException {
		return getElementMetadata(id, DataElement.class);
	}

	private Response getElementMetadata(String id, Class<? extends Element> elementSubType) throws FemmeApplicationException {
		FemmeResponse<MetadataList> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<MetadataList> entity = new FemmeResponseEntity<>();

		try {
			Element element = this.femme.get(id, elementSubType);

			if (element == null) {
				throw new FemmeApplicationException("No " + elementSubType.getSimpleName() + " with id " + id + " found", Response.Status.NOT_FOUND.getStatusCode());
			}

			MetadataList metadata = new MetadataList(element.getMetadata());
			//entity.setHref(this.uriInfo.getBaseUriBuilder().path(this.uriInfo.getPath()).toString()).setBody(metadata);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(metadata.getSize() + " metadata found").setEntity(entity);

		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().entity(femmeResponse).build();
	}
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.METADATA_PATH + "/{metadatumId}")
	public Response getCollectionMetadatum(@PathParam("collectionId") String collectionId, @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return getElementMetadatum(collectionId, metadatumId, Collection.class);
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeResource.METADATA_PATH + "/{metadatumId}")
	public Response getDataElementMetadatum(@PathParam("dataElementId") String dataElementId, @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return getElementMetadatum(dataElementId, metadatumId, DataElement.class);
	}

	private Response getElementMetadatum(String elementId, String metadatumId, Class<? extends Element> elementSubType) throws FemmeApplicationException {
		FemmeResponse<Metadatum> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Metadatum> entity = new FemmeResponseEntity<>();

		try {
			Element element = this.femme.get(elementId, elementSubType);

			if (element == null) {
				throw new FemmeApplicationException("No " + elementSubType.getSimpleName() + " with id " + elementId + " found", Response.Status.NOT_FOUND.getStatusCode());
			}

			Metadatum metadatum = element.getMetadata().stream().filter(metadatumRequested -> metadatumRequested.getId().equals(metadatumId)).findFirst()
					.orElseThrow(() -> new FemmeApplicationException("No metadatum with id " + metadatumId + " found", Response.Status.NOT_FOUND.getStatusCode()));

			entity/*.setHref(this.uriInfo.getBaseUriBuilder().path(this.uriInfo.getPath()).toString())*/.setBody(metadatum);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Metadatum " + metadatumId + " found").setEntity(entity);

		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.METADATA_PATH + "/{metadatumId}/value")
	public Response getCollectionMetadatumValue(@PathParam("collectionId") String collectionId, @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return getElementMetadatumValue(collectionId, metadatumId, Collection.class);
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/{dataElementId}/" + FemmeResource.METADATA_PATH + "/{metadatumId}/value")
	public Response getDataElementMetadatumValue(@PathParam("dataElementId") String dataElementId, @PathParam("metadatumId") String metadatumId) throws FemmeApplicationException {
		return getElementMetadatumValue(dataElementId, metadatumId, DataElement.class);
	}

	private Response getElementMetadatumValue(String elementId, String metadatumId, Class<? extends Element> elementSubType) throws FemmeApplicationException {
		/*FemmeResponse<String> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<String> entity = new FemmeResponseEntity<>();*/

		Metadatum metadatum;
		try {
			Element element = this.femme.get(elementId, elementSubType);

			if (element == null) {
				throw new FemmeApplicationException("No " + elementSubType.getSimpleName() + " with id " + elementId + " found", Response.Status.NOT_FOUND.getStatusCode());
			}

			metadatum = element.getMetadata().stream().filter(metadatumRequested -> metadatumRequested.getId().equals(metadatumId)).findFirst()
					.orElseThrow(() -> new FemmeApplicationException("No metadatum with id " + metadatumId + " found", Response.Status.NOT_FOUND.getStatusCode()));
			/*entity.setBody(metadatum.getValue());*/
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().type(metadatum.getContentType()).entity(metadatum.getValue()).build();
	}

	private static void prettifyMetadata(List<Metadatum> metadata) {
		metadata.forEach(metadatum -> {
			if (metadatum.getContentType().toLowerCase().contains("xml") && metadatum.getValue() != null && !metadatum.getValue().trim().isEmpty()) {
				try {
					metadatum.setValue(XMLFormatter.indent(metadatum.getValue()));
				} catch (TransformerException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	
}
