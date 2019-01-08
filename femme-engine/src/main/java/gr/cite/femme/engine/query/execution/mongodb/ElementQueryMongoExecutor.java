/*
package gr.cite.femme.engine.query.execution.mongodb;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import gr.cite.femme.core.datastores.DatastoreRepositoryProvider;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.engine.datastore.mongodb.repositories.MongoDatastoreRepository;
import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementQueryMongoExecutor<T extends Element> implements QueryExecutor<T> {
	private static final Logger logger = LoggerFactory.getLogger(QueryMongoExecutor.class);
	
	private DatastoreRepositoryProvider datastoreRepositoryProvider;
	
	private MongoCollection<T> collection;
	private Document queryDocument;
	private QueryOptionsMessenger options;
	private FindIterable<T> results;
	private boolean lazyMetadata = false;
	//private long totalQueryStart;
	
	
	QueryMongoExecutor(DatastoreRepositoryProvider datastoreRepositoryProvider, Class<T> elementSubtype) {
		this.datastoreRepositoryProvider = datastoreRepositoryProvider;
	}

	*/
/*public QueryMongoExecutor(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		this.datastore = datastore;
		this.collection = this.datastore.getRepository(elementSubtype);
		this.metadataStore = metadataStore;
	}*//*

	
	boolean isLazyMetadata() {
		*/
/*if (this.options != null) {
			if (this.options.getInclude() != null && !this.options.getInclude().contains("metadata")) {
				this.lazyMetadata = true;
			}
			if (this.options.getExclude() != null && this.options.getExclude().contains("metadata")) {
				this.options.getExclude().remove("metadata");
				this.lazyMetadata = true;
			}
		}*//*

		return this.lazyMetadata;
	}
	
	public QueryOptionsMessenger getOptions() {
		return options;
	}
	
	protected void setLazyMetadata(boolean lazyMetadata) {
		this.lazyMetadata = lazyMetadata;
	}
	
	Document getQueryDocument() {
		return queryDocument;
	}
	
	void setQueryDocument(Document queryDocument) {
		this.queryDocument = queryDocument;
	}
	
	public QueryExecutor<T> options(QueryOptionsMessenger options) {
		this.options = options;
		
		if (options != null) {
			if (options.getInclude() != null && !options.getInclude().contains("metadata")) {
				this.lazyMetadata = true;
			}
			if (options.getExclude() != null && options.getExclude().contains("metadata")) {
				options.getExclude().remove("metadata");
				this.lazyMetadata = true;
			}
		}
		return this;
	}
	
	@Override
	public QueryExecutor<T> find(Query<? extends Criterion> query) {
		//totalQueryStart = System.currentTimeMillis();
		if (query != null) {
			this.queryDocument = postProcessQuery((QueryMongo) query);
		}
		
		return this;
	}
	
	@Override
	public long count(Query<? extends Criterion> query) {
		if (query != null) {
			this.queryDocument = postProcessQuery((QueryMongo) query);
		}
		
		return this.collection.count(
			this.queryDocument == null
				? Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
				: Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
		);
	}
	
	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		List<T> elements = new ArrayList<>();
		
		if (this.options == null || this.options.getLimit() == null || this.options.getLimit() > 0) {
			this.results = this.collection.find(
				this.queryDocument == null
					? Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
					: Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
			);
			
			if (this.results != null && this.options != null) {
				if (options.getLimit() != null) {
					this.results.limit(options.getLimit());
				}
				if (options.getOffset() != null) {
					this.results.skip(options.getOffset());
				}
				if (options.getAsc() != null) {
					this.results.sort(Sorts.ascending(options.getAsc()));
				}
				if (options.getDesc() != null) {
					this.results.sort(Sorts.descending(options.getDesc()));
				}
				
				List<Bson> projections = new ArrayList<>();
				if (options.getInclude() != null) {
					projections.addAll(options.getInclude().stream().map(field -> "id".equals(field) ? FieldNames.ID : field).map(Projections::include).collect(Collectors.toList()));
				}
				if (options.getExclude() != null) {
					projections.addAll(options.getExclude().stream().map(field -> "id".equals(field) ? FieldNames.ID : field).map(Projections::exclude).collect(Collectors.toList()));
				}
				if (options.getInclude() != null || options.getInclude() != null) {
					this.results.projection(Projections.fields(projections));
				}
			}
			this.results.into(elements);
			
			//logger.info("Total query time: " + (System.currentTimeMillis() - this.totalQueryStart) + " ms");
		}
		
		return elements;
	}
	
	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		if (this.options == null || this.options.getLimit() == null || this.options.getLimit() > 0) {
			this.results = this.collection.find(
				this.queryDocument == null
					? Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
					: Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
			).limit(1);
			return this.results.first();
		}
		return null;
	}
	
	
	private Document postProcessQuery(QueryMongo query) {
		Document queryDocument = null;
		if (query != null) {
			queryDocument = query.build().get();
			Document inclusionOperatorDocument = findInclusionOperator(queryDocument);
			
			if (inclusionOperatorDocument != null) {
				List<Collection> collections;
				
				//System.out.println(inclusionOperatorDocument.get("$in_any_collection"));
				
				//List<Document> docs = postProcessIdField((Object)inclusionOperatorDocument.get("$in_any_collection"));
				
				//System.out.println(docs);
				
				collections = ((MongoDatastoreRepository<Collection>) this.datastoreRepositoryProvider.get(Collection.class))
								  .find(new Document("$or", inclusionOperatorDocument.get("$in_any_collection")));
				inclusionOperatorDocument.remove("$in_any_collection");
				System.out.println(queryDocument);
				
				List<String> collectionIds = collections.stream().map(Element::getId).collect(Collectors.toList());
				
				inclusionOperatorDocument.append(FieldNames.COLLECTIONS, new Document("$all", collectionIds));
			}
		}
		
		return queryDocument;
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
		Class clazz = document.getClass();
		
		if (Document.class.equals(clazz)) {
			for (Map.Entry<String, Object> doc : ((Document) document).entrySet()) {
				if (doc.getKey().equals("$in_any_collection")) {
					inclusion = (Document) document;
					return inclusion;
				} else {
					if (inclusion != null) {
						return inclusion;
					}
					inclusion = findInclusionOperator(doc.getValue());
				}
			}
		} else if (List.class.equals(clazz)) {
			for (Document doc : (List<Document>)document) {
				if (inclusion != null) {
					return inclusion;
				}
				inclusion = findInclusionOperator(doc);
			}
		}
		return inclusion;
	}

	*/
/*public static <U extends Element>QueryMongoExecutor.QueryExecutionBuilder<U> builder(MongoDatastore datastore, Class<U> elementSubtype) {
		return new QueryMongoExecutor.QueryExecutionBuilder<>(datastore, elementSubtype);
	}

	public static class QueryExecutionBuilder<T extends Element> {
		private QueryMongoExecutor<T> queryExecutor;

		private QueryExecutionBuilder(MongoDatastore datastore, Class<T> elementSubtype) {
			queryExecutor = new QueryMongoExecutor<>(datastore, elementSubtype);
		}

		public QueryMongoExecutor.QueryExecutionBuilder options(QueryOptionsMessenger options) {
			queryExecutor.options(options);
			return this;
		}

		public QueryMongoExecutor<T> build() {
			return queryExecutor;
		}
	}*//*

}
*/
