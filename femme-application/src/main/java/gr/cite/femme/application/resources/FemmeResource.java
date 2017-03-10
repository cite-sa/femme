package gr.cite.femme.application.resources;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
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

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.femme.application.exception.FemmeApplicationException;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.dto.CollectionList;
import gr.cite.femme.dto.DataElementList;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.dto.FemmeResponseEntity;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.query.mongodb.CriterionMongo;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;
import gr.cite.femme.query.api.QueryOptionsMessenger;
import gr.cite.femme.query.mongodb.QueryMongo;

@Component
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeResource {

	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);
	
	@Context
	private UriInfo uriInfo;
	
	private Datastore<Criterion, Query<Criterion>> datastore;

	@Inject
	public FemmeResource(Datastore<Criterion, Query<Criterion>> datastore) {
		this.datastore = datastore;
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
		
		QueryExecutor<Collection> queryExecutor = datastore.find(query, Collection.class).options(options);
		
		try {
			List<Collection> collections = queryExecutor.list();

			if (collections.isEmpty()) {
				logger.error("No collections found");
				throw new FemmeApplicationException("No collections found for this query", Response.Status.NOT_FOUND.getStatusCode());
			}
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage(collections.size() + " collections found").setEntity(entity.setBody(new CollectionList(collections)));

		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new FemmeApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e);
		}
		
		return Response.ok().entity(femmeResponse).build();

	}
	
	@GET
	@Path("collections/{id}")
	public Response getCollectionById(@PathParam("id") String id) throws FemmeApplicationException {
		Collection collection;
		FemmeResponse<Collection> femmeResponse = new FemmeResponse<>();
		FemmeResponseEntity<Collection> entity = new FemmeResponseEntity<>();
		
		try {
			collection = datastore.getCollection(id);
			
			if (collection == null) {
				throw new FemmeApplicationException("No collection with id " + id + " found", Response.Status.NOT_FOUND.getStatusCode());	
			}
			entity.setHref(uriInfo.getRequestUri().toString()).setBody(collection);
			femmeResponse.setStatus(Response.Status.OK.getStatusCode()).setMessage("Collection " + id + " found").setEntity(entity);
			
		} catch (DatastoreException e) {
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
		long count = datastore.count(query, Collection.class);
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
		
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			QueryExecutor<DataElement> queryExecutor = datastore.find(query, DataElement.class).xPath(xPath).options(options);
			
			/*List<DataElement> dataElements = xPath != null && !xPath.equals("") ? queryOption.xPath(xPath) : queryExecutor.list();*/
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
			QueryExecutor<DataElement> queryExecutor = datastore.find(query, DataElement.class).xPath(xPath).options(options);
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
			@PathParam("collectionId") String collectionId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {
		
		
		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		
		try {
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().inAnyCollection(Arrays.asList(
							CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end()))
					.end());
			QueryExecutor<DataElement> queryExecutor = datastore.find(query, DataElement.class).xPath(xPath).options(options);
			
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
	
	@GET
	@Path("collections/{collectionId}/dataElements/dataElementId")
	public Response getDataElementsInCollection(
			@PathParam("collectionId") String collectionId,
			@PathParam("dataElementId") String dataElementId,
			@QueryParam("options") QueryOptionsMessenger options,
			@QueryParam("xpath") String xPath) throws FemmeApplicationException {

		FemmeResponse<DataElementList> femmeResponse = new FemmeResponse<>();
		try {
			CriterionMongo collectionCriterion = CriterionBuilderMongo.root().inAnyCollection(Arrays.asList(
					CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(collectionId)).end())).end();
			CriterionMongo dataElementCriterion = CriterionBuilderMongo.root().eq(FieldNames.ID, dataElementId).end();
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().and(Arrays.asList(collectionCriterion, dataElementCriterion)).end());
			
			QueryExecutor<DataElement> queryExecutor = datastore.find(query, DataElement.class).options(options);
			
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
	
	@GET
	@Path("dataElements/{id}")
	public Response getDataElementById(@PathParam("id") String id) throws FemmeApplicationException {

		FemmeResponse<DataElement> femmeResponse = new FemmeResponse<>();
		
		try {
			QueryExecutor<DataElement> query = datastore.find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()), DataElement.class);
			QueryOptionsMessenger options = new QueryOptionsMessenger();
			options.setLimit(1);
			DataElement dataElement = query.options(options).first();
			
			if (dataElement == null) {
				logger.info("No DataElement with id " + id + " found");
				throw new FemmeApplicationException("No data element with id " + id + " found", Response.Status.NOT_FOUND.getStatusCode());
			}
			
			femmeResponse.setStatus(Response.Status.OK.getStatusCode())
				.setMessage("Data element " + dataElement.getId() + " found")
				.setEntity(new FemmeResponseEntity<>(uriInfo.getRequestUri().toString(), dataElement));
			logger.info("DataElement " + id + " found");
			
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
		long count = datastore.count(query, DataElement.class);
		femmeResponse.setStatus(Response.Status.OK.getStatusCode())
			.setMessage(count +" data elements found")
			.setEntity(new FemmeResponseEntity<Long>(uriInfo.getRequestUri().toString(), count));
		
		return Response.ok().entity(femmeResponse).build();

	}
	
}
