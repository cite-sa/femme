package gr.cite.femme.engine.query.mongodb;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;
import gr.cite.femme.core.query.api.QueryExecutor;
import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;

import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;

public class QueryMongoExecutor<T extends Element> implements QueryExecutor<T> {
	private static final Logger logger = LoggerFactory.getLogger(QueryMongoExecutor.class);

	private MongoDatastore datastore;
	//private MetadataStore metadataStore;
	private MongoCollection<T> collection;
	private Document queryDocument;
	private QueryOptionsMessenger options;
	private FindIterable<T> results;
	private boolean lazyMetadata = false;
	private Instant totalQueryDuration;


	public QueryMongoExecutor(MongoDatastore datastore, Class<T> elementSubtype) {
		this.datastore = datastore;
		this.collection = this.datastore.getCollection(elementSubtype);
	}

	/*public QueryMongoExecutor(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		this.datastore = datastore;
		this.collection = this.datastore.getCollection(elementSubtype);
		this.metadataStore = metadataStore;
	}*/

	boolean isLazyMetadata() {
		return lazyMetadata;
	}

	protected void setLazyMetadata(boolean lazyMetadata) {
		this.lazyMetadata = lazyMetadata;
	}

	public Document getQueryDocument() {
		return queryDocument;
	}

	public void setQueryDocument(Document queryDocument) {
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
			/*if (this.results != null) {
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
			}*/
		}
		return this;
	}

	@Override
	public QueryExecutor<T> find(Query<? extends Criterion> query) {
		totalQueryDuration = Instant.now();
		if (query != null) {
			this.queryDocument = postProcessQuery((QueryMongo) query, datastore);
			logger.debug("Query: " + queryDocument.toJson());
		}
		return this;
	}

	/*@Override
	public QueryExecutor<T> xPath(String xPath, MetadataStore metadataStore) throws DatastoreException, MetadataStoreException {
		if (xPath != null && !xPath.trim().equals("")) {
			List<Metadatum> metadataXPathResults;

			Duration xPathQueryDuration;
			Instant xPathQueryStart = Instant.now();

			metadataXPathResults = this.metadataStore.xPath(xPath);

			Instant xPathQueryEnd = Instant.now();
			xPathQueryDuration = Duration.between(xPathQueryStart, xPathQueryEnd);
			logger.info("XPath query duration: " + xPathQueryDuration.toMillis() + "ms");

			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID,
						new Document().append("$in",
								metadataXPathResults.stream()*//*.filter(metadatum -> metadatum.getId() != null)*//*
										.map(metadatum -> new ObjectId(metadatum.getElementId()))
										.distinct().collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			this.queryDocument = this.queryDocument == null
					? retrieveXPathSatisfyElementsQuery
					: new Document().append("$and", Arrays.asList(
							this.queryDocument,
							retrieveXPathSatisfyElementsQuery,
							Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
						));
		}
		return this;
	}*/

	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		List<T> elements = new ArrayList<>();

		/*logger.debug("Total elements in memory XPath: " + (this.queryDocument == null ? this.collection.count()
				: this.collection.count(Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument))));*/

		this.results = this.queryDocument == null ? this.collection.find()
		: this.collection.find(Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument));

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

		//options(options);

		/*if (lazyMetadata) {
			MongoCursor<T> cursor;
			try {
				cursor = this.results.iterator();
			} catch (MongoQueryException e) {
				throw new DatastoreException("Query failed: [" + (this.queryDocument == null ? "find all" : this.queryDocument.toJson()) + "]", e);
			}

			ExecutorService executor = Executors.newFixedThreadPool(10);
			List<Future<T>> futures = new ArrayList<>();
			try {
				while (cursor.hasNext()) {
					T element = cursor.next();
					futures.add(executor.submit(() -> {
                        element.setMetadata(this.metadataStore.find(element.getId()));
                        logger.debug("Element " + element.getId() +" found");
                        return element;
                    }));
				}
			} finally {
				for(Future<T> future : futures) {
					try {
						elements.add(future.get());
					} catch (InterruptedException | ExecutionException e) {
						logger.error(e.getMessage(), e);
					}
				}
				cursor.close();
			}
		} else {*/
		this.results.into(elements);
		//}
		logger.info("Total query duration: " + Duration.between(totalQueryDuration, Instant.now()).toMillis() + "ms");

		return elements;
	}

	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		results = this.queryDocument == null ? collection.find().limit(1) : collection.find(this.queryDocument).limit(1);
		//options(options);
		//T element = results.first();
		/*if (element != null && lazyMetadata) {
			element.setMetadata(this.metadataStore.find(element.getId()));
		}*/
		return results.first();
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
			for (Entry<String, Object> doc : ((Document) document).entrySet()) {
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

	public static <U extends Element>QueryMongoExecutor.QueryExecutionBuilder<U> builder(MongoDatastore datastore, Class<U> elementSubtype) {
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
	}
}
