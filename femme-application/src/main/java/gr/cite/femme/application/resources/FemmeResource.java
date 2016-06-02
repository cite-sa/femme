package gr.cite.femme.application.resources;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IQuery;
import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Query;
import gr.cite.femme.query.mongodb.QueryOptions;
import gr.cite.femme.query.serialization.mongodb.CriteriaSerializer;
import gr.cite.femme.query.serialization.mongodb.QuerySerializer;

@Component
@Path("femme")
public class FemmeResource {
	private static final Logger logger = LoggerFactory.getLogger(FemmeResource.class);
	
	private Datastore<Criteria, Query> datastore;
	
	@Inject
	public FemmeResource(Datastore<Criteria, Query> datastore) {
		this.datastore = datastore;
	}


	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@POST
	@Path("collections")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Collection collection) {
		Collection insertedCollection = null;
		try {
			insertedCollection = datastore.insert(collection);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
		
		return Response.ok(insertedCollection.getId()).build();
	}
	
	@POST
	@Path("collections/{collectionId}/dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCollection(@PathParam("collectionId") String collectionId, DataElement dataElement) {
		DataElement insertedDataElement = null;
		try {
			insertedDataElement = datastore.addToCollection(dataElement, collectionId);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
		
		return Response.ok(insertedDataElement.getId()).build();
	}
	
	/*@POST
	@Path("collections")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(List<Collection> collections) {
		List<Collection> insertedCollections = null;
		
		try {
			insertedCollections = datastore.insert(collections);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
		
		return Response.ok(insertedCollections.stream().map(collection -> collection.getId()).collect(Collectors.toList())).build();
	}*/
	
	@GET
	@Path("collections/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollection(@PathParam("id") String id) {
		return Response.ok(datastore.getCollection(id)).build();
	}
	
	@POST
	@Path("dataElements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(DataElement dataElement) {
		DataElement insertedDataElement = null;
		try {
			insertedDataElement = datastore.insert(dataElement);
		} catch (DatastoreException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
		
		return Response.ok(insertedDataElement.getId()).build();
	}
	
	@POST
	@Path("dataElements/{collectionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertInCollection(@PathParam("collectionId") String collectionId, DataElement dataElement) {
		DataElement insertedDataElement = null;
		
		try {
			insertedDataElement = datastore.addToCollection(dataElement, Criteria.query().where(FieldNames.ID).eq(collectionId));
		} catch (DatastoreException | InvalidCriteriaQueryOperation e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage());
		}
		
		return Response.ok(insertedDataElement.getId()).build();
	}
	
	
	@GET
	@Path("dataElements/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataElement(@PathParam("id") String id) {
		return Response.ok(datastore.getDataElement(id)).build();
	}
	
	@GET
	@Path("dataElements")
	@Produces(MediaType.APPLICATION_JSON)
	public Response find(
			@QueryParam("query") QuerySerializer query,
			@QueryParam("limit") int limit,
			@QueryParam("xpath") String xpath) {
		
		Query finalQuery = query == null ? null : query.build();
		return Response.ok(datastore.find(finalQuery, DataElement.class).list()).build();
	}
}
