package gr.cite.femme.engine.query.execution.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.datastores.DatastoreRepositoryProvider;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.core.query.execution.QueryExecutor;
import gr.cite.femme.engine.datastore.mongodb.repositories.MongoDatastoreRepository;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Projections;

import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.model.Collection;

public class QueryMongoExecutor<T extends Element> implements QueryExecutor<T> {
	private static final Logger logger = LoggerFactory.getLogger(QueryMongoExecutor.class);

	private DatastoreRepositoryProvider datastoreRepositoryProvider;
	private MongoDatastoreRepository<T> datastoreRepository;
	
	private Bson queryDocument;
	private QueryOptionsMessenger options;
	private FindIterable<T> results;
	private boolean lazyMetadata = false;

	QueryMongoExecutor(DatastoreRepositoryProvider datastoreRepositoryProvider, Class<T> elementSubtype) {
		this.datastoreRepositoryProvider = datastoreRepositoryProvider;
		this.datastoreRepository = (MongoDatastoreRepository<T>) this.datastoreRepositoryProvider.get(elementSubtype);
		
	}
	
	boolean isLazyMetadata() {
		return this.lazyMetadata;
	}

	public QueryOptionsMessenger getOptions() {
		return options;
	}

	protected void setLazyMetadata(boolean lazyMetadata) {
		this.lazyMetadata = lazyMetadata;
	}

	Bson getQueryDocument() {
		return queryDocument;
	}

	void setQueryDocument(Bson queryDocument) {
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
		if (query != null) this.queryDocument = postProcessQuery((QueryMongo) query);
		return this;
	}

	@Override
	public long count(Query<? extends Criterion> query) {
		if (query != null) {
			this.queryDocument = postProcessQuery((QueryMongo) query);
		}

		
		return this.datastoreRepository.count(
			this.queryDocument == null
				? Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
				: Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
		);
	}

	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		if (this.options == null || this.options.getLimit() == null || this.options.getLimit() > 0) {
			this.results = this.datastoreRepository.lookup(
					this.queryDocument == null
							? Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
							: Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
			);
			applyOptions();
			return this.results.into(new ArrayList<>());
		}

		return new ArrayList<>();
	}
	
	private void applyOptions() {
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
	}

	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		if (this.options == null || this.options.getLimit() == null || this.options.getLimit() > 0) {
			this.results = this.datastoreRepository.lookup(
					this.queryDocument == null ?
						Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()) :
						Filters.and(Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode()), this.queryDocument)
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
				List<Collection> collections = ((MongoDatastoreRepository<Collection>) this.datastoreRepositoryProvider.get(Collection.class))
												   .lookup(new Document("$or", inclusionOperatorDocument.get("$in_any_collection")))
												   .projection(Projections.include(FieldNames.ID)).into(new ArrayList<>());
				inclusionOperatorDocument.remove("$in_any_collection");
				logger.debug(queryDocument.toJson());

				List<String> collectionIds = collections.stream().map(Element::getId).collect(Collectors.toList());
				inclusionOperatorDocument.append(FieldNames.COLLECTIONS, new Document("$all", collectionIds));
			}
		}

		return queryDocument;
	}

	private List<Document> postProcessIdField(Object document) {
		List<Document> docs = new ArrayList<>();
		if (document.getClass().getSimpleName().contains("List")) {
			for (Document doc : (List<Document>) document) {
				Document idDoc = (Document)(doc.get("_id"));
				
				/*String id = (String)(idDoc.get("$eq"));
				idDoc.remove("$eq");
				idDoc.append("$eq", new ObjectId(id));*/
				String id = (String) idDoc.get("$eq");
				idDoc.replace("$eq", new ObjectId(id));

				docs.add(doc);
			}
		}
		return docs;
	}

	private Document findInclusionOperator(Object document) {
		Document inclusion = null;
		Class clazz = document.getClass();

		if (Document.class.equals(clazz)) {
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

}
