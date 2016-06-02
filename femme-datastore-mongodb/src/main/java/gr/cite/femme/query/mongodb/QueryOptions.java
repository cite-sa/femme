package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.query.IQueryOptions;

public class QueryOptions<T extends Element> implements IQueryOptions<T> {
	private static final Logger logger = LoggerFactory.getLogger(QueryOptions.class);
	
	MongoDatastore datastore;
	
	MongoCollection<T> collection;
	
	MetadataStore metadataStore;
	
	private FindIterable<T> results;
	
	
	public QueryOptions() {
		
	}

	public QueryOptions(MongoCollection<T> collection) {
		this.collection = collection;
		this.results = collection.find();
	}
	
	public QueryOptions(Query query, MongoDatastore datastore, Class<T> elementSubtype) {
			this.datastore = datastore;
			
			if (elementSubtype == DataElement.class) {
				collection = (MongoCollection<T>) datastore.getDataElements();
				
				if (query != null && !query.isCollectionsResolved() && query.getQuery().containsKey("collections")) {
					Map<String, Object> collectionsIn = (Map<String, Object>) query.getQuery().get("collections");
					
					/*Criteria collectionCriteria = (Criteria) collectionElemMatch.get("$in");
					List<Document> colls = findCollections(collectionCriteria).stream().map(new Function<Collection, Document>() {
						@Override
						public Document apply(Collection coll) {
							return new Document().append("_id", new ObjectId(coll.getId()));
						}
					}).collect(Collectors.toList());
					collectionElemMatch.put("$in", colls);*/
					
					List<Document> resolvedCollections = new ArrayList<>();
					datastore.getCollections().find(new Document(((Criteria)collectionsIn.get("$in")).getCriteria()))
					.map(collection -> {
						return new Document().append("_id", new ObjectId(collection.getId()));
					}).into(resolvedCollections);
					
					collectionsIn.put("$in", resolvedCollections);
					query.resolveCollections();
				}
				
			} else if (elementSubtype == Collection.class) {
				collection = (MongoCollection<T>) datastore.getCollections();
				
				if (query != null && !query.isDataElementsResolved() && query.getQuery().containsKey("dataElements")) {
					Map<String, Object> dataElementsIn = (Map<String, Object>) query.getQuery().get("dataElements");
					
					List<Document> resolvedDataElements = new ArrayList<>();
					datastore.getDataElements().find(new Document(((Criteria)dataElementsIn.get("$in")).getCriteria()))
					.map(dataElement -> {
						return new Document().append("_id", new ObjectId(dataElement.getId()));
					}).into(resolvedDataElements);
				
					dataElementsIn.put("$in", resolvedDataElements);
					query.resolveCollections();
				}
			}
			
			metadataStore = datastore.getMetadataStore();
			results = query == null ? this.collection.find() : this.collection.find(new Document(query.getQuery())); 
	}
	
	@Override
	public IQueryOptions<T> count() {
		return this;
	}

	@Override
	public IQueryOptions<T> limit(int limit) {
		results.limit(limit);
		return this;
	}
	
	@Override
	public IQueryOptions<T> skip(int skip) {
		results.skip(skip);
		return this;
	}
	
	@Override
	public IQueryOptions<T> sort(String field, String order) throws InvalidCriteriaQueryOperation {
		int orderNum = 0;
		if (!(order.equals("asc") || order.equals("desc"))) {
			throw new InvalidCriteriaQueryOperation("Sort accepts only asc or desc order.");
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
	
	public List<T> list() {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) results.iterator();
		try {
			while (cursor.hasNext()) {
				elements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return elements;
	}
	
	public MongoCursor<T> iterator() {
		return results.iterator();
	}
	
	@Override
	public List<T> xPath(String xPath) {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) results.iterator();
		try {
			while (cursor.hasNext()) {
				T element = cursor.next();
				if (metadataStore.find(element, xPath) != null) {
					elements.add(element);					
				}
				/*elements.add(cursor.next());*/
			}
		}  catch (MetadataStoreException e) {
			logger.error(e.getMessage(), e);
		} finally {
			cursor.close();
		}
		/*List<T> xPathedElements = null;
		try {
			xPathedElements = metadataStore.find(elements, xPath);
		} catch (MetadataStoreException e) {
			logger.error(e.getMessage(), e);
		}*/
		return elements;
	}
}
