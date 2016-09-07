package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.InvalidQueryOperation;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.QueryOptions;

public class QueryOptionsMongo<T extends Element> implements QueryOptions<T> {

private static final Logger logger = LoggerFactory.getLogger(QueryOptionsMongo.class);
	
	private MongoDatastore datastore;
	
	private MongoCollection<T> collection;
	
	private MetadataStore metadataStore;
	
	private FindIterable<T> results;
	
	public QueryOptionsMongo() {
		// TODO Auto-generated constructor stub
	}
	
	public QueryOptionsMongo(MongoCollection<T> collection) {
		this.collection = collection;
		this.results = collection.find();
	}
	
	public QueryOptionsMongo(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
			this.datastore = datastore;
			
			if (elementSubtype == DataElement.class) {
				collection = (MongoCollection<T>) datastore.getDataElements();
				
				/*if (query != null && !query.isCollectionsResolved() && query.getQuery().containsKey("collections")) {
					Map<String, Object> collectionsIn = (Map<String, Object>) query.getQuery().get("collections");
					
					List<Document> resolvedCollections = new ArrayList<>();
					datastore.getCollections().find(new Document(((CriteriaOldMongo)collectionsIn.get("$in")).getCriteria()))
					.map(collection -> {
						return new Document().append("_id", new ObjectId(collection.getId()));
					}).into(resolvedCollections);
					
					collectionsIn.put("$in", resolvedCollections);
					query.resolveCollections();
				}*/
				
			} else if (elementSubtype == Collection.class) {
				collection = (MongoCollection<T>) datastore.getCollections();
				
				/*if (query != null && !query.isDataElementsResolved() && query.getQuery().containsKey("dataElements")) {
					Map<String, Object> dataElementsIn = (Map<String, Object>) query.getQuery().get("dataElements");
					
					List<Document> resolvedDataElements = new ArrayList<>();
					datastore.getDataElements().find(new Document(((CriteriaOldMongo)dataElementsIn.get("$in")).getCriteria()))
					.map(dataElement -> {
						return new Document().append("_id", new ObjectId(dataElement.getId()));
					}).into(resolvedDataElements);
				
					dataElementsIn.put("$in", resolvedDataElements);
					query.resolveCollections();
				}*/
			}
			
			metadataStore = datastore.getMetadataStore();
			results = query == null ? this.collection.find() : this.collection.find(query.build()); 
			
			if (query != null) {
				logger.debug("Query: " + query.build().toJson());
			}
	}
	
	@Override
	public QueryOptions<T> limit(Integer limit) {
		if (limit != null) {
			results.limit(limit);
		}
		return this;
	}

	@Override
	public QueryOptions<T> skip(Integer skip) {
		if (skip != null) {
			results.skip(skip);
		}
		return this;
	}

	@Override
	public QueryOptions<T> sort(String field, String order) throws InvalidQueryOperation {
		int orderNum = 0;
		if (!(order.equals("asc") || order.equals("desc"))) {
			throw new InvalidQueryOperation("Sort accepts only asc or desc order.");
		}
		if (order.equals("asc")) {
			orderNum = 1;
		} else if (order.equals("desc")) {
			orderNum = -1;
		}
		Document sortDocument = new Document().append(field, orderNum); 
		results.sort(sortDocument);
		
		return this;
	}

	@Override
	public List<T> list() throws DatastoreException {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) results.iterator();
		
		List<Future<T>> futures = new ArrayList<Future<T>>();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		try {
			while (cursor.hasNext()) {
				T element = cursor.next();
				futures.add(executor.submit(new Callable<T>() {
					@Override
					public T call() throws Exception {
						element.setMetadata(metadataStore.find(element.getId()));
						logger.debug("Element " + element.getName() +" found");
						return element;
					}
				}));
			}
		} finally {
			for(Future<T> future : futures) {
				try {
					elements.add(future.get());
				} catch (InterruptedException e) {
					cursor.close();
					logger.error(e.getMessage(), e);
					throw new DatastoreException(e.getMessage(), e);
				} catch (ExecutionException e) {
					cursor.close();
					logger.error(e.getMessage(), e);
					throw new DatastoreException(e.getMessage(), e);
				}
			}
			cursor.close();
		}
		return elements;
	}

	@Override
	public T first() throws DatastoreException {
		T element = results.first();
		try {
			element.setMetadata(metadataStore.find(element.getId()));
		} catch (MetadataStoreException e) {
			logger.error(e.getMessage(), e);
			throw new DatastoreException(e.getMessage(), e);
		}
		return element;
	}

	@Override
	public List<T> xPath(String xPath) throws DatastoreException {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) results.iterator();
		
		List<Future<T>> futures = new ArrayList<Future<T>>();
		ExecutorService executor = Executors.newFixedThreadPool(50);
		
		try {
			while (cursor.hasNext()) {
				T element = cursor.next();
				
				futures.add(executor.submit(new Callable<T>() {
	
					@Override
					public T call() throws Exception {
						if (metadataStore.xPath(element, xPath) != null) {
							return element;					
						}
						return null;
					}
				}));
				
				/*if (metadataStore.find(element, xPath) != null) {
					elements.add(element);					
				}*/
			}
		/*}  catch (MetadataStoreException e) {
			logger.error(e.getMessage(), e);*/
		} finally {
			executor.shutdown();
			
			for(Future<T> future : futures) {
				try {
					elements.add(future.get());
				} catch (InterruptedException e) {
					cursor.close();
					logger.error(e.getMessage(), e);
					throw new DatastoreException(e.getMessage(), e);
				} catch (ExecutionException e) {
					cursor.close();
					logger.error(e.getMessage(), e);
					throw new DatastoreException(e.getMessage(), e);
				}
			}
			
			cursor.close();
		}
		
		return elements;
	}

}
