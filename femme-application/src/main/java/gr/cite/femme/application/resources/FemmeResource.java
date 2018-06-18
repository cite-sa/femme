package gr.cite.femme.application.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import gr.cite.femme.core.query.execution.MetadataQueryExecutorBuilder;
import gr.cite.femme.engine.Femme;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
//import gr.cite.femme.core.dto.DataElementList;
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
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("pretty") boolean pretty,
			@DefaultValue("false") @QueryParam("loadInactiveMetadata") boolean loadInactiveMetadata) throws FemmeApplicationException {

		return getElementById(Collection.class, id, xPath, options, pretty, loadInactiveMetadata);
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
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/name/{name}")
	public Response xpathDataElementWithName(@NotNull @PathParam("name") String name, @QueryParam("xpath") String xPath) throws FemmeApplicationException {
		return xPathDataElementWithName(name, xPath, false);
	}
	
	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/name/{name}/xpath")
	public Response xPathInMemoryDataElementWithName(@NotNull @PathParam("name") String name, @QueryParam("xpath") String xPath) throws FemmeApplicationException {
		return xPathDataElementWithName(name, xPath, true);
	}
	
	private Response xPathDataElementWithName(String name, String xPath, boolean xPathInMemory) throws FemmeApplicationException {
		FemmeResponse<DataElement> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<DataElement> entity = new FemmeResponseEntity<>();
		
		try {
			MetadataQueryExecutorBuilder.FindQueryExecutorBuilder<DataElement> dataElementQuery = this.femme.query(DataElement.class)
					.find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.NAME, name).end()));
			
			if (xPath != null && ! xPath.trim().isEmpty()) {
				if (xPathInMemory)
					dataElementQuery.xPathInMemory(xPath);
				else
					dataElementQuery.xPath(xPath);
			}
			
			DataElement dataElement = dataElementQuery.execute().first();
			if (dataElement == null) {
				throw new FemmeApplicationException("No DataElement [" + name + "] found", Response.Status.NOT_FOUND.getStatusCode());
			}
			
			entity.setBody(dataElement);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("DataElement [" + name + "] found").setEntity(entity);
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
		
		logger.info(query == null ? "Query all DataElements" : "Query DataElements: " + query.build());

		FemmeResponse<ElementList<DataElement>> femmeResponse = new FemmeResponse<>();
		List<DataElement> dataElements;
		try {
			long start = System.currentTimeMillis();
			dataElements = this.femme.query(DataElement.class).find(query).xPathInMemory(xPath).options(options).execute().list();
			long end = System.currentTimeMillis();
			logger.info("In memory XPath total time: " + (end - start) + " ms");
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		String message;
		if (dataElements.isEmpty()) {
			message = "No data elements found";
			logger.info(message);
			throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
		} else {
			message = dataElements.size()+ " data elements found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message)
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), new ElementList<>(dataElements)));
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/{id}")
	public Response getDataElementById(
			@NotNull @PathParam("id") String id,
			@QueryParam("xpath") String xPath,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("pretty") boolean pretty,
			@DefaultValue("false") @QueryParam("loadInactiveMetadata") boolean loadInactiveMetadata) throws FemmeApplicationException {

		return getElementById(DataElement.class, id, xPath, options, pretty, loadInactiveMetadata);
	}

	private Response findElements(Class<? extends Element> elementType, QueryMongo query, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {
		logger.debug(query == null ? "Query all " + elementType.getSimpleName() : "Query " +  elementType.getSimpleName() + " : " + query.build());

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

	private Response findQuery(Class<? extends Element> elementType, Query<? extends Criterion> query, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {
		FemmeResponse<ElementList<? extends Element>> femmeResponse = new FemmeResponse<>();
		List<? extends Element> elements;
		try {
			long start = System.currentTimeMillis();

			elements = this.femme.query(elementType).find(query).options(options).xPath(xPath).execute().list();

			long end = System.currentTimeMillis();
			logger.info("XPath total time: " + (end - start) + " ms");

			if (pretty) {
				elements.forEach(element -> prettifyMetadata(element.getMetadata()));
			}
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		String message;
		if (elements.isEmpty()) {
			message = "No " + elementType.getSimpleName() + " found";
			logger.info(message);
			throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
		} else {
			message = elements.size() + " " + elementType.getSimpleName() + " found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(new FemmeResponseEntity<>(new ElementList<>(elements)));
		}

		return Response.ok().entity(femmeResponse).build();
	}

	private Response getElement(Class<? extends Element> elementType, Query<? extends Criterion> query, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {

		FemmeResponse<Element> femmeResponse = new FemmeResponse<>();
		Element element;
		try {
			element = this.femme.query(elementType).find(query).options(options).xPath(xPath).execute().first();
			if (pretty) {
				prettifyMetadata(element.getMetadata());
			}

			String message;
			if (element == null) {
				message = "No " + elementType.getSimpleName() + " found";
				logger.info(message);
				throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
			}

			message = elementType.getSimpleName() + " found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(new FemmeResponseEntity<>(element));
		} catch (DatastoreException | MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}

	private Response getElementById(Class<? extends Element> elementType, String id, String xPath, QueryOptionsMessenger options, boolean pretty, boolean loadInactiveMetadata) throws FemmeApplicationException {
		FemmeResponse<Element> femmeResponse = new FemmeResponse<>();
		Element element;

		try {
			element = this.femme.get(elementType, id, xPath, options, loadInactiveMetadata);
			if (pretty) {
				prettifyMetadata(element.getMetadata());
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
	public Response findDataElementsByIds(@QueryParam("id") List<String> ids, @QueryParam("options") QueryOptionsMessenger options, @QueryParam("xpath") String xPath) throws FemmeApplicationException {
		
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
		
		FemmeResponse<ElementList<DataElement>> femmeResponse = new FemmeResponse<>();
		try {
			List<DataElement> dataElements = this.femme.query(DataElement.class).find(query).xPath(xPath).options(options).execute().list();
			
			ElementList<DataElement> dataElementList = new ElementList<>(dataElements);
			
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
	
	@POST
	@Path(FemmeResource.DATA_ELEMENTS_PATH + "/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response findDataElementsByIdsInBody(List<String> ids, @QueryParam("options") QueryOptionsMessenger options, @QueryParam("xpath") String xPath) throws FemmeApplicationException {
		FemmeResponse<ElementList<DataElement>> femmeResponse = new FemmeResponse<>();
		try {
			if (options != null) {
				if (options.getOffset() != null && options.getLimit() != null) {
					int offset = options.getOffset() < ids.size() ? options.getOffset() : ids.size();
					int limit = options.getOffset() + options.getLimit() < ids.size() ? options.getOffset() + options.getLimit() : ids.size();
					ids = ids.subList(offset, limit);
				}
			}
			
			List<DataElement> dataElements = ids.stream().map(id -> {
				try {
					return this.femme.get(DataElement.class, id, xPath);
				} catch (DatastoreException | MetadataStoreException e) {
					logger.error(e.getMessage(), e);
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
			
			ElementList<DataElement> dataElementList = new ElementList<>(dataElements);
			
			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				femmeResponse.setStatus(Response.Status.OK.getStatusCode())
					.setMessage(dataElementList.getSize() + " data elements found")
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElementList));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		
		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.DATA_ELEMENTS_PATH)
	public Response getDataElementsInCollection(@NotNull @PathParam("collectionId") String collectionId, @QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath, @QueryParam("pretty") boolean pretty) throws FemmeApplicationException {
		
		return getDataElementsInCollectionWithFieldValue(FieldNames.ID, this.femme.generateId(collectionId), options, xPath, pretty);
	}

	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{field}/{value}/" + FemmeResource.DATA_ELEMENTS_PATH)
	public Response getDataElementsInCollectionWithField(@PathParam("field") String field, @PathParam("value") String value, @QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath, @QueryParam("pretty") boolean pretty) throws FemmeApplicationException {

		if (!FieldNames.NAME.equals(field)) {
			throw new FemmeApplicationException("Unsupported field " + field, Response.Status.BAD_REQUEST.getStatusCode());
		}
		return getDataElementsInCollectionWithFieldValue(field, value, options, xPath, pretty);
	}

	private Response getDataElementsInCollectionWithFieldValue(String field, Object value, QueryOptionsMessenger options, String xPath, boolean pretty) throws FemmeApplicationException {
		Query<? extends Criterion> query = QueryMongo.query().addCriterion(
				CriterionBuilderMongo.root().inAnyCollection(Collections.singletonList(
						CriterionBuilderMongo.root().eq(field, value).end()
				)).end());

		return findQuery(DataElement.class, query, options, xPath, pretty);
	}
	
	@GET
	@Path(FemmeResource.COLLECTIONS_PATH + "/{collectionId}/" + FemmeResource.DATA_ELEMENTS_PATH  + "/{dataElementId}")
	public Response getDataElementsInCollection(@NotNull @PathParam("collectionId") String collectionId, @NotNull @PathParam("dataElementId") String dataElementId,
			@QueryParam("options") QueryOptionsMessenger options, @QueryParam("xpath") String xPath, @QueryParam("pretty") boolean pretty) throws FemmeApplicationException {

		CriterionMongo collectionCriterion = CriterionBuilderMongo.root().inAnyCollection(Collections.singletonList(
				CriterionBuilderMongo.root().eq(FieldNames.ID, this.femme.generateId(collectionId)).end())).end();

		CriterionMongo dataElementCriterion = CriterionBuilderMongo.root().eq(FieldNames.ID, this.femme.generateId(dataElementId)).end();

		Query<? extends Criterion> query = QueryMongo.query().addCriterion(
				CriterionBuilderMongo.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());

		return getElement(DataElement.class, query, options, xPath, pretty);
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
		//FemmeResponseEntity<MetadataList> entity = new FemmeResponseEntity<>();

		try {
			Element element = this.femme.get(elementSubType, id);
			if (element == null) {
				throw new FemmeApplicationException("No " + elementSubType.getSimpleName() + " with id " + id + " found", Response.Status.NOT_FOUND.getStatusCode());
			}
			List<Metadatum> metadata = this.femme.getElementMetadata(id);

			//entity/*.setHref(this.uriInfo.getBaseUriBuilder().path(this.uriInfo.getPath()).toString())*/.setBody(new MetadataList(element.getMetadata()));
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(metadata.size() + " metadata found")
					.setEntity(new FemmeResponseEntity<>(new MetadataList(metadata)));
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
			Element element = this.femme.get(elementSubType, elementId);

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
			Element element = this.femme.get(elementSubType, elementId);
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

	private void prettifyMetadata(List<Metadatum> metadata) {
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
	
	private FemmeApplicationException buildFemmeApplicationException(String message, Response.Status status, Exception exception) {
		logger.error(message, exception);
		return new FemmeApplicationException(message, status.getStatusCode(), exception);
	}

	
}
