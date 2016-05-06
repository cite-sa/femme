package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import gr.cite.femme.core.Element;
import gr.cite.femme.datastore.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.datastore.mongodb.gridfs.MetadatumGridFS;
import gr.cite.femme.query.IQuery;
import gr.cite.femme.query.IQueryOptions;

public class QueryOptions<T extends Element> implements IQueryOptions<T> {
	MongoCollection<T> collection;
	
	MetadatumGridFS metadatumGridFS;
	
	private FindIterable<T> query;
	
	
	public QueryOptions() {
		
	}

	public QueryOptions(MongoCollection<T> collection) {
		this.collection = collection;
		this.query = collection.find();
	}
	
	public QueryOptions(MongoCollection<T> collection, MetadatumGridFS metadatumGridFS, IQuery query) {
		this.collection = collection;
		this.metadatumGridFS = metadatumGridFS;
		this.query = collection.find(new Document(query.getQuery()));
	}
	
	@Override
	public IQueryOptions<T> count() {
		return this;
	}

	@Override
	public IQueryOptions<T> limit(int limit) {
		query.limit(limit);
		return this;
	}
	
	@Override
	public IQueryOptions<T> skip(int skip) {
		query.skip(skip);
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
			query.sort(sortDocument);
		return this;
	}
	
	public List<T> list() {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) query.iterator();
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
		return query.iterator();
	}

	@Override
	public List<T> xPath(String xPath) {
		List<T> elements = new ArrayList<>();
		MongoCursor<T> cursor = (MongoCursor<T>) query.iterator();
		try {
			while (cursor.hasNext()) {
				elements.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return metadatumGridFS.find(elements, xPath);
	}
}
