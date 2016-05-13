package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.exceptions.IllegalElementSubtype;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.exceptions.MetadataStoreException;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.metadata.MetadataGridFS;
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
	
	public QueryOptions(Query query, MongoDatastore datastore, Class<T> elementSubtype) throws IllegalElementSubtype {
		if (query instanceof Query) {
			Query theQuery = (Query) query;
			this.datastore = datastore;
			
			String subtype = elementSubtype.getSimpleName();
			if (subtype.equals("DataElement")) {
				collection = (MongoCollection<T>) datastore.getDataElements();
				
				if (!theQuery.isCollectionsResolved() && theQuery.getQuery().containsKey("collections")) {
					Map<String, Object> collectionsIn = (Map<String, Object>) theQuery.getQuery().get("collections");
					
					/*Criteria collectionCriteria = (Criteria) collectionElemMatch.get("$in");
					List<Document> colls = findCollections(collectionCriteria).stream().map(new Function<Collection, Document>() {
						@Override
						public Document apply(Collection coll) {
							return new Document().append("_id", new ObjectId(coll.getId()));
						}
					}).collect(Collectors.toList());
					collectionElemMatch.put("$in", colls);*/
					
					collectionsIn.put("$in", 
							findCollections((Criteria)collectionsIn.get("$in")).stream()
							.map(new Function<Collection, Document>() {
								@Override
								public Document apply(Collection coll) {
									return new Document().append("_id", new ObjectId(coll.getId()));
								}
							}).collect(Collectors.toList()));
					
					theQuery.resolveCollections();
				}
				
			} else if (subtype.equals("Collection")) {
				collection = (MongoCollection<T>) datastore.getCollections();
				
				if (!theQuery.isDataElementsResolved() && theQuery.getQuery().containsKey("dataElements")) {
					Map<String, Object> dataElementsIn = (Map<String, Object>) theQuery.getQuery().get("dataElements");
					
					dataElementsIn.put("$in", 
							findDataElements((Criteria)dataElementsIn.get("$in")).stream()
							.map(new Function<DataElement, Document>() {
								@Override
								public Document apply(DataElement dataEl) {
									return new Document().append("_id", new ObjectId(dataEl.getId()));
								}
							}).collect(Collectors.toList()));
					
					theQuery.resolveCollections();
				}
			} else {
				throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype.");
			}

			
			metadataStore = datastore.getMetadataStore();
			results = this.collection.find(new Document(theQuery.getQuery()));
		} else {
			throw new IllegalArgumentException("Argument must be instance of Criteria class");
		}
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
				elements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		List<T> xPathedElements = null;
		try {
			xPathedElements = metadataStore.find(elements, xPath);
		} catch (MetadataStoreException e) {
			logger.error(e.getMessage(), e);
		}
		return xPathedElements;
	}
	
	public List<Collection> findCollections(Criteria criteria) {
		List<Collection> collections = new ArrayList<>();
		MongoCursor<Collection> cursor = datastore.getCollections().find(new Document(criteria.getCriteria())).iterator();
		try {
			while (cursor.hasNext()) {
				collections.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return collections;
	}
	
	public List<DataElement> findDataElements(Criteria criteria) {
		List<DataElement> dataElements = new ArrayList<>();
		MongoCursor<DataElement> cursor = datastore.getDataElements().find(new Document(criteria.getCriteria())).iterator();
		try {
			while (cursor.hasNext()) {
				dataElements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return dataElements;
	}
}
