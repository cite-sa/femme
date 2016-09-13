package gr.cite.femme.application.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

@Component
@Path("femme")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeResource {
	
	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);
	
	private Datastore<Criterion, Query<Criterion>> datastore;

	@Inject
	public FemmeResource(Datastore<Criterion, Query<Criterion>> datastore) {
		this.datastore = datastore;
	}
	
	
	@POST
	@Path("collections")
	public FemmeResponse<CollectionList> findCollections(
			QueryMongo query,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xpath) {

		FemmeResponse<CollectionList> response = new FemmeResponse<>();
		
		QueryOptions<Collection> queryOptions = datastore.find(null, Collection.class);
		
		if (limit != null) {
			queryOptions.limit(limit);
		}
		if (offset != null) {
			queryOptions.skip(offset);
		}
		
		try {
			List<Collection> collections = queryOptions.list();
			response.setStatus(true).setMessage("ok").setEntity(new CollectionList(collections));
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			response.setStatus(false).setMessage(e.getMessage());
		}
		
		return response;

	}
	
	@GET
	@Path("collections/{id}")
	public FemmeResponse<Collection> getCollection(@PathParam("id") String id) {
		Collection collection = null;
		FemmeResponse<Collection> response = new FemmeResponse<>();
		try {
			collection = datastore.getCollection(id);
			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(collection);
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}
		return response;

	}
	
	@POST
	@Path("dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<DataElementList> findDataElements(
			Query<Criterion> query,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset,
			@QueryParam("xpath") String xpath) {

		FemmeResponse<DataElementList> response = new FemmeResponse<>();
		
		QueryOptions<DataElement> queryOptions = datastore.find(query, DataElement.class);
		if (limit != null) {
			queryOptions.limit(limit);
		}
		if (offset != null) {
			queryOptions.skip(offset);
		}

		
		try {
			List<DataElement> dataElements = queryOptions.list();
			response.setStatus(true).setMessage("ok").setEntity(new DataElementList(dataElements));
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			response.setStatus(false).setMessage(e.getMessage());
		}
		
		return response;

	}
	
	/*@GET
	@Path("collections/{collectionId}/dataElements")
	public FemmeResponse<DataElementList> getDataElements(
			@PathParam("collectionId") String collectionId,
			@QueryParam("limit") Integer limit,
			@QueryParam("offset") Integer offset) {
		List<DataElement> dataElements = null;
		FemmeResponse<DataElementList> response = new FemmeResponse<>();
		
		try {
			Query<? extends Criterion> query = QueryMongo.query().addCriterion(CriterionBuilderMongo.root().end());inCollection(Lists.newArrayList("collectionId"))
			dataElements = datastore.
					find(query, DataElement.class).limit(limit).skip(offset).list();
			
			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(new DataElementList(dataElements));
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;
	}
	
	@GET
	@Path("dataElements/{id}")
	public FemmeResponse<DataElement> getDataElementById(@PathParam("id") String id) {

		QueryOptions<DataElement> query = null;
		FemmeResponse<DataElement> response = new FemmeResponse<>();
		
		try {
			query = datastore.find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, id).end()),
					DataElement.class);
			
			DataElement dataElement = query.limit(1).first();

			response.setStatus(true);
			response.setMessage("ok");
			response.setEntity(dataElement);
		} catch (DatastoreException e) {
			response.setStatus(false);
			response.setMessage(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return response;

	}

	@POST
	@Path("dataElements/count")
	@Consumes(MediaType.APPLICATION_JSON)
	public FemmeResponse<Long> countDataElements(
			QueryMongo query,
			@QueryParam("xpath") String xpath) {

		FemmeResponse<Long> response = new FemmeResponse<>();
		
		long count = datastore.count(query, DataElement.class);
		response.setStatus(true).setMessage("ok").setEntity(count);
		
		return response;

	}*/
	
}
