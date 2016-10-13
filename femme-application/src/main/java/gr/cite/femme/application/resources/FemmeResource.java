package gr.cite.femme.application.resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.dto.CollectionList;
import gr.cite.femme.dto.DataElementList;
import gr.cite.femme.dto.FemmeResponse;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.query.mongodb.CriterionMongo;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryOptions;
import gr.cite.femme.query.mongodb.QueryMongo;
import gr.cite.femme.query.mongodb.QueryOptionsMongo;

@Component
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeResource {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	
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
	/*@Consumes(MediaType.APPLICATION_JSON)*/
	public FemmeResponse<CollectionList> findCollections(
			@QueryParam("query") String queryJson,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xpath) {
		
		QueryMongo query = null;
		if (queryJson != null) {
			try {
				query = objectMapper.readValue(queryJson, QueryMongo.class);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		FemmeResponse<CollectionList> response = new FemmeResponse<>();
		
		QueryOptions<Collection> queryOptions = datastore.find(query, Collection.class).limit(limit).skip(offset);
		
		try {
			List<Collection> collections = queryOptions.list();
			response.setStatus(true).setMessage("ok").setEntity(new CollectionList(collections));
			if (query == null) {
				logger.info("Query all Collections");
			} else {				
				logger.info("Query on Collections: " + query.build());
			}
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			response.setStatus(false).setMessage(e.getMessage());
		}
		
		return response;

	}
	
	@GET
	@Path("collectionsJSONP")
	@Produces("application/javascript")
	public JSONPObject findCollectionsJSONP(
			@QueryParam("query") String queryJson,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xpath,
			@DefaultValue("callback") @QueryParam("callback") String callback) {

		return new JSONPObject(callback, findCollections(queryJson, limit, offset, xpath));

	}
	
	@GET
	@Path("collections/{id}")
	public FemmeResponse<Collection> getCollectionById(@PathParam("id") String id) {
		Collection collection = null;
		FemmeResponse<Collection> response = new FemmeResponse<>();
		try {
			collection = datastore.getCollection(id);
			response.setStatus(true).setMessage("ok").setEntity(collection);
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}
		return response;

	}
	
	@GET
	@Path("collectionsJSONP/{id}")
	@Produces("application/javascript")
	public JSONPObject getCollectionByIdJSONP(@PathParam("id") String id, @DefaultValue("callback") @QueryParam("callback") String callback) {
		return new JSONPObject(callback, getCollectionById(id));

	}
	
	@GET
	@Path("collections/count")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<Long> countCollections(
			@QueryParam("query") String queryJson,
			@QueryParam("xpath") String xpath) {
		
		QueryMongo query = null;
		if (queryJson != null) {
			try {
				query = objectMapper.readValue(queryJson, QueryMongo.class);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		FemmeResponse<Long> response = new FemmeResponse<>();
		
		// TODO Add XPath
		long count = datastore.count(query, Collection.class);
		response.setStatus(true).setMessage("ok").setEntity(count);
		
		return response;

	}
	
	@GET
	@Path("dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<DataElementList> findDataElements(
			@QueryParam("query") String queryJson,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xPath) {

		QueryMongo query = null;
		if (queryJson != null) {
			try {
				query = objectMapper.readValue(queryJson, QueryMongo.class);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		FemmeResponse<DataElementList> response = new FemmeResponse<>();
		
		QueryOptions<DataElement> queryOptions = datastore.find(query, DataElement.class).limit(limit).skip(offset);
		
		try {
			List<DataElement> dataElements = null;
			if (xPath != null && !xPath.equals("")) {
				dataElements = queryOptions.xPath(xPath);				
			} else {
				dataElements = queryOptions.list();
			}
			 
			response.setStatus(true).setMessage("ok").setEntity(new DataElementList(dataElements));
			if (query == null) {
				logger.info("Query all DataElements");
			} else {
				logger.info("Query on DataElements: " + query.build());
			}
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			response.setStatus(false).setMessage(e.getMessage());
		}
		
		return response;

	}
	
	@GET
	@Path("dataElementsJSONP")
	@Produces("application/javascript")
	public JSONPObject findDataElementsJSONP(
			@QueryParam("query") String queryJson,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xPath,
			@DefaultValue("callback") @QueryParam("callback") String callback) {
		return new JSONPObject(callback, findDataElements(queryJson, limit, offset, xPath));
	}
	
	@GET
	@Path("collections/{collectionId}/dataElements")
	public FemmeResponse<DataElementList> getDataElementsInCollection(
			@PathParam("endpoint") String collectionEndpoint,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xPath) {
		
		List<DataElement> dataElements = null;
		FemmeResponse<DataElementList> response = new FemmeResponse<>();
		
		try {
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(
					CriterionBuilderMongo.root().inAnyCollection(Arrays.asList(
							CriterionBuilderMongo.root().eq(FieldNames.ENDPOINT, collectionEndpoint).end()))
					.end());
			QueryOptions<DataElement> queryOptions = datastore.find(query, DataElement.class).limit(limit).skip(offset);
			
			if (xPath != null && !xPath.equals("")) {
				dataElements = queryOptions.xPath(xPath);
			} else {
				dataElements = queryOptions.list();
			}
			
			response.setStatus(true).setMessage("ok").setEntity(new DataElementList(dataElements));
		} catch (DatastoreException e) {
			response.setStatus(false).setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;
	}
	
	@GET
	@Path("collections/{collectionId}/dataElementsJSONP")
	@Produces("application/javascript")
	public JSONPObject getDataElementsInCollectionJSONP(
			@PathParam("endpoint") String collectionEndpoint,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xPath,
			@DefaultValue("callback") @QueryParam("callback") String callback) {
		return new JSONPObject(callback, getDataElementsInCollection(collectionEndpoint, limit, offset, xPath));
	}
	
	@GET
	@Path("dataElements/{id}")
	public FemmeResponse<DataElement> getDataElementById(@PathParam("id") String id) {

		QueryOptions<DataElement> query = null;
		FemmeResponse<DataElement> response = new FemmeResponse<>();
		
		try {
			query = datastore.find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId(id)).end()),
					DataElement.class);
			
			DataElement dataElement = query.limit(1).first();
			response.setStatus(true).setMessage("ok").setEntity(dataElement);
			
			logger.info("Find DataElement " + id);
		} catch (DatastoreException e) {
			response.setStatus(false).setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;

	}
	
	@GET
	@Path("dataElementsJSONP/{id}")
	@Produces("application/javascript")
	public JSONPObject getDataElementByIdJSONP(
			@PathParam("id") String id,
			@DefaultValue("callback") @QueryParam("callback") String callback) {
		return new JSONPObject(callback, getDataElementById(id));
	}

	@GET
	@Path("dataElements/count")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<Long> countDataElements(
			@QueryParam("query") String queryJson,
			@QueryParam("xpath") String xpath) {
		
		QueryMongo query = null;
		if (queryJson != null) {
			try {
				query = objectMapper.readValue(queryJson, QueryMongo.class);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		FemmeResponse<Long> response = new FemmeResponse<>();
		
		// TODO Add XPath
		long count = datastore.count(query, DataElement.class);
		response.setStatus(true).setMessage("ok").setEntity(count);
		
		return response;

	}
	
}
