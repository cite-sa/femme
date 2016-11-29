package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoQueryException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;

import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.InvalidQueryOperation;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.QueryOptions;
import gr.cite.femme.query.api.QueryOptionsFields;

@JsonInclude(Include.NON_EMPTY)
public class QueryOptionsMongo<T extends Element> implements QueryOptions<T> {

	private static final Logger logger = LoggerFactory.getLogger(QueryOptionsMongo.class);
	
	private MongoDatastore datastore;
	
	private MongoCollection<T> collection;
	
	private MetadataStore metadataStore;
	
	private FindIterable<T> results;
	
	private Boolean lazyMetadata = false;
	
	public QueryOptionsMongo() {
		
	}
	
	public QueryOptionsMongo(MongoCollection<T> collection) {
		this.collection = collection;
		this.results = collection.find();
	}
	
	public QueryOptionsMongo(QueryMongo query, MongoDatastore datastore, Class<T> elementSubtype) {
			this.datastore = datastore;
			
			Document queryDocument = postProcessQuery(query, datastore);
			
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
//			results = query == null ? this.collection.find() : this.collection.find(query.build()); 
			results = this.collection.find(queryDocument);
			
			if (query != null) {
				logger.info("Query: " + queryDocument.toJson());
			}
	}

	public QueryOptions<T> options(QueryOptionsFields options) {
		if (options != null) {
			if (options.getLimit() != null) {
				results.limit(options.getLimit());
			}
			if (options.getOffset() != null) {
				results.skip(options.getOffset());
			}
			if (options.getAsc() != null) {
				results.sort(new Document(options.getAsc(), 1));
			}
			if (options.getDesc() != null) {
				results.sort(new Document(options.getDesc(), -1));
			}
			if (options.getInclude() != null) {
				results.projection(Projections.include(new ArrayList<>(options.getInclude())));
				if (!options.getInclude().contains("metadata")) {
					lazyMetadata = true;
				}
			}
			if (options.getExclude() != null) {
				results.projection(Projections.exclude(new ArrayList<>(options.getExclude())));
				if (options.getExclude().contains("metadata")) {
					lazyMetadata = true;
				}
			}
		}
		
		return this;
	}
	
	/*@Override
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
	public QueryOptions<T> asc(String field) {
		if (field != null) {
			results.sort(new Document(field, 1));
		}
		return this;
	}
	
	@Override
	public QueryOptions<T> desc(String field) {
		if (field != null) {
			results.sort(new Document(field, -1));
		}
		return this;
	}
	
	@Override
	public QueryOptions<T> include(String ...fields) {
		results.projection(Projections.include(fields));
		return this;
	}
	
	@Override
	public QueryOptions<T> exclude(String ...fields) {
		lazyMetadata = Arrays.asList(fields).contains("metadata") ? true : false;
		results.projection(Projections.exclude(fields));
		return this;
	}*/

	@Override
	public List<T> list() throws DatastoreException {
		List<T> elements = new ArrayList<>();
		
		if (!lazyMetadata) {
			MongoCursor<T> cursor = null;
			try {				
				cursor = (MongoCursor<T>) results.iterator();
			} catch (MongoQueryException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException(e.getMessage(), e);
			}
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
		} else {
			results.into(elements);
		}
		return elements;
	}

	@Override
	public T first() throws DatastoreException {
		T element = results.first();
		if (element != null) {
			try {
				element.setMetadata(metadataStore.find(element.getId()));
			} catch (MetadataStoreException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException(e.getMessage(), e);
			}
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
	
	private Document postProcessQuery(QueryMongo query, MongoDatastore datastore) {
		if (query != null) {
			Document queryDocument = query.build();
			Document inclusionOperatorDocument = findInclusionOperator(queryDocument);
			
			if (inclusionOperatorDocument != null) {
				List<Collection> collections = new ArrayList<>();
				
				//System.out.println(inclusionOperatorDocument.get("$in_any_collection"));
				
				//List<Document> docs = postProcessIdField((Object)inclusionOperatorDocument.get("$in_any_collection"));
				
				//System.out.println(docs);
				
				datastore.getCollections().find(new Document("$or", inclusionOperatorDocument.get("$in_any_collection")))
					/*.projection(Projections.include(FieldNames.ID))*/
					.into(collections);
				inclusionOperatorDocument.remove("$in_any_collection");
				System.out.println(queryDocument);
				
				List<ObjectId> collectionIds = collections.stream().map(collection -> new ObjectId(collection.getId())).collect(Collectors.toList());
				
				inclusionOperatorDocument.append(FieldNames.DATA_ELEMENT_COLLECTION_ID, new Document("$in", collectionIds));
			}
			
			return queryDocument;
		} else {
			return new Document();
		}
		
	}
	
	private List<Document> postProcessIdField(Object document) {
		List<Document> docs = new ArrayList<>();
		if (document.getClass().getSimpleName().contains("List")) {
			for (Document doc : (List<Document>)document) {
				Document idDoc = (Document)(doc.get("_id"));
				String id = (String)(idDoc.get("$eq"));
				idDoc.remove("$eq");
				idDoc.append("$eq", new ObjectId(id));
				
				docs.add(doc);
				
			}
		}
		return docs;
	}
	
	private Document findInclusionOperator(Object document) {
		
		Document inclusion = null;
		String className = document.getClass().getSimpleName();
		
		if (className.equals("Document")) {
			Iterator<Entry<String, Object>> iterator = ((Document)document).entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> doc = iterator.next();
				if (doc.getKey().equals("$in_any_collection")) {
					
					System.out.println("found");
					
					inclusion = (Document)document;
					return inclusion;
				} else {
					if (inclusion != null) {
						return inclusion;
					}
					inclusion = findInclusionOperator(doc.getValue());
				}
			}
		} else if (className.contains("List")) {
			for (Document doc : (List<Document>)document) {
				if (inclusion != null) {
					return inclusion;
				}
				inclusion = findInclusionOperator(doc);
			}
		}
		
		return inclusion;
		
	}

}
