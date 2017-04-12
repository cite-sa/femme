package gr.cite.femme.application.resources;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import gr.cite.femme.engine.Femme;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.dto.CollectionList;
import gr.cite.femme.core.dto.DataElementList;
import gr.cite.femme.core.dto.FemmeResponse;
import gr.cite.femme.core.dto.FemmeResponseEntity;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.engine.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.engine.query.mongodb.CriterionMongo;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.query.api.QueryExecutor;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;
import gr.cite.femme.engine.query.mongodb.QueryMongo;

@Component
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);
	
	@Context
	private UriInfo uriInfo;
	private Femme femme;

	@Inject
	public FemmeResource(Femme femme) {
		this.femme = femme;
	}
	
	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@GET
	@Path("collections")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findCollections(
			@QueryParam("query") QueryMongo query,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		
		if (query == null) {
			logger.info("Query all Collections");
		} else {				
			logger.info("Query on Collections: " + query.build());
		}
		
		FemmeResponse<CollectionList> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<CollectionList> entity = new FemmeResponseEntity<>();
		
		//QueryExecutor<Collection> queryExecutor = datastore.get(query, Collection.class).options(options);
		QueryExecutor<Collection> queryExecutor = this.femme.find(query, Collection.class).options(options);
		
		try {
			List<Collection> collections = queryExecutor.list();

			String message;
			if (collections.isEmpty()) {
				message = "No collections found";
				logger.error(message);
				throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
			}
			message = collections.size() + " collections found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message).setEntity(entity.setBody(new CollectionList(collections)));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
		
		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path("collections/{id}")
	public Response getCollectionById(@NotNull @PathParam("id") String id) throws FemmeApplicationException {
		FemmeResponse<Collection> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Collection> entity = new FemmeResponseEntity<>();
		
		try {
			//collection = datastore.getCollection(id);
			Collection collection = this.femme.find(id, Collection.class);
			
			if (collection == null) {
				throw new FemmeApplicationException("No collection with id " + id + " found", Response.Status.NOT_FOUND.getStatusCode());	
			}
			entity.setHref(uriInfo.getRequestUri().toString()).setBody(collection);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Collection " + id + " found").setEntity(entity);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}

		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path("collections/count")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response countCollections(
			@QueryParam("query") QueryMongo query,
			@QueryParam("xpath") String xpath) throws FemmeApplicationException {
		
		FemmeResponse<Long> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Long> entity = new FemmeResponseEntity<>();

		// TODO Add XPath
		long count = this.femme.count(query, Collection.class);
		femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(count + " collections found").setEntity(entity.setBody(count));
		
		return Response.status(femmeResponse.getStatus()).entity(femmeResponse).build();

	}
	
	@GET
	@Path("dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response findDataElements(
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
			//QueryExecutor<DataElement> queryExecutor = datastore.get(query, DataElement.class).xPath(xPath).options(options);
			QueryExecutor<DataElement> queryExecutor = this.femme.find(query, DataElement.class).xPath(xPath).options(options);
			//QueryExecutor<DataElement> queryExecutor = this.femme.get(query, DataElement.class).xPath(xPath).options(options);

			/*List<DataElement> dataElements = xPath != null && !xPath.equals("") ? queryOption.xPath(xPath) : queryExecutor.list();*/
			List<DataElement> dataElements = queryExecutor.list();
			dataElementList = new DataElementList(dataElements);

		} catch (Exception e) {
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
	@Path("dataElements/{id}")
	public Response getDataElementById(@NotNull @PathParam("id") String id) throws FemmeApplicationException {

		FemmeResponse<DataElement> femmeResponse = new FemmeResponse<>();

		try {
			/*QueryExecutor<DataElement> query = datastore.get(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), DataElement.class);
			QueryOptionsMessenger options = new QueryOptionsMessenger();
			options.setLimit(1);
			DataElement dataElement = query.options(options).first();*/

			DataElement dataElement = this.femme.find(id, DataElement.class);

			String message;
			if (dataElement == null) {
				message = "No DataElement with id " + id + " found";
				logger.info(message);
				throw new FemmeApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
			}

			message = "DataElement " + dataElement.getId() + " found";
			logger.info(message);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(message)
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElement));
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}

	@GET
	@Path("dataElements/count")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response countDataElements(
			@QueryParam("query") QueryMongo query,
			@QueryParam("xpath") String xpath) throws FemmeApplicationException {

		if (query == null) {
			logger.info("Count all DataElements");
		} else {
			logger.info("Count on DataElements: " + query.build());
		}

		FemmeResponse<Long> femmeResponse = new FemmeResponse<>();

		// TODO Add XPath
		long count = this.femme.count(query, DataElement.class);
		femmeResponse.setStatus(Response.Status.OK.getStatusCode())
				.setMessage(count +" data elements found")
				.setEntity(new FemmeResponseEntity<Long>(uriInfo.getRequestUri().toString(), count));

		return Response.ok().entity(femmeResponse).build();

	}

	@GET
	@Path("dataElements/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response findDataElementsByIds(
			@QueryParam("id") List<String> ids,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		
		QueryMongo query = null;
		if (ids.size() > 0) {
			List<CriterionMongo> idsCriteria = ids.stream().map(id -> CriterionBuilderMongo.root().eq("_id", new ObjectId(id)).end()).collect(Collectors.toList());
			query = QueryMongo.query().addCriterion(CriterionBuilderMongo.root().or(idsCriteria).end());
		}
		if (query == null) {
			logger.info("Query all DataElements");
		} else {
			logger.info("Query on DataElements: " + query.build());
		}
		
		
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			//QueryExecutor<DataElement> queryExecutor = datastore.get(query, DataElement.class).xPath(xPath).options(options);
			QueryExecutor<DataElement> queryExecutor = this.femme.find(query, DataElement.class).xPath(xPath).options(options);
//			List<DataElement> dataElements = xPath != null && !xPath.equals("") ? queryExecutor.xPath(xPath) : queryExecutor.list();
			List<DataElement> dataElements = queryExecutor.list();
			DataElementList dataElementList = new DataElementList(dataElements);
			
			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				femmeResponse.setStatus(Response.Status.OK.getStatusCode())
					.setMessage(dataElementList.getSize() + " data elements found")
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElementList));
			}
			 
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		
		return Response.ok().entity(femmeResponse).build();

	}
	
	@GET
	@Path("collections/{collectionId}/dataElements")
	public Response getDataElementsInCollection(
			@NotNull @PathParam("collectionId") String collectionId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		return getDataElementsInCollectionWithFieldValue(FieldNames.ID, collectionId, options, xPath);
	}

	@GET
	@Path("collections/{field}/{value}/dataElements")
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
					CriterionBuilderMongo.root().inAnyCollection(Arrays.asList(
							CriterionBuilderMongo.root().eq(field, FieldNames.ID.equals(field) ? new ObjectId(value) : value).end()
					)).end());
			//QueryExecutor<DataElement> queryExecutor = datastore.get(query, DataElement.class).xPath(xPath).options(options);
			QueryExecutor<DataElement> queryExecutor = this.femme.find(query, DataElement.class).xPath(xPath).options(options);

//			List<DataElement> dataElements = xPath != null && !xPath.equals("") ? queryExecutor.xPath(xPath) : queryExecutor.list();
			List<DataElement> dataElements = queryExecutor.list();
			DataElementList dataElementList = new DataElementList(dataElements);

			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode())
					.setMessage(dataElementList.getSize() + " data elements found")
					.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElementList));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}
	
	@GET
	@Path("collections/{collectionId}/dataElements/{dataElementId}")
	public Response getDataElementsInCollection(
			@NotNull @PathParam("collectionId") String collectionId,
			@NotNull @PathParam("dataElementId") String dataElementId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {

		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			CriterionMongo collectionCriterion = CriterionBuilderMongo.root().inAnyCollection(Arrays.asList(
					CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end())).end();
			CriterionMongo dataElementCriterion = CriterionBuilderMongo.root().eq(FieldNames.ID, dataElementId).end();
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
			
			//QueryExecutor<DataElement> queryExecutor = datastore.get(query, DataElement.class).options(options);
			QueryExecutor<DataElement> queryExecutor = this.femme.find(query, DataElement.class).options(options);

//			List<DataElement> dataElements = xPath != null && !xPath.equals("") ? queryExecutor.xPath(xPath) : queryExecutor.list();
			List<DataElement> dataElements = queryExecutor.list();
			DataElementList dataElementList = new DataElementList(dataElements);
			
			if (dataElementList.getSize() == 0) {
				logger.info("No data elements found");
				throw new FemmeApplicationException("No data elements found", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode())
				.setMessage(dataElementList.getSize() + " data elements found")
				.setEntity(new FemmeResponseEntity<DataElementList>(uriInfo.getRequestUri().toString(), dataElementList));
			
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return Response.ok().entity(femmeResponse).build();
	}
	
}
