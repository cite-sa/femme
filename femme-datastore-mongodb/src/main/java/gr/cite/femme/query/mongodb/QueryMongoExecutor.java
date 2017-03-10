package gr.cite.femme.query.mongodb;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.mongodb.client.model.Sorts;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoQueryException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;

import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.MongoDatastore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.query.api.QueryOptionsMessenger;

public class QueryMongoExecutor<T extends Element> implements QueryExecutor<T> {

	private static final Logger logger = LoggerFactory.getLogger(QueryMongoExecutor.class);
	
	private MongoDatastore datastore;
	private MetadataStore metadataStore;
	private MongoCollection<T> collection;
	private Document queryDocument;
	private QueryOptionsMessenger options;
	private FindIterable<T> results;
	private boolean loadMetadata = true;
	private Instant totalQueryDuration;


	public QueryMongoExecutor(MongoDatastore datastore, Class<T> elementSubtype) {
		this.datastore = datastore;
		if (elementSubtype == DataElement.class) {
			this.collection = (MongoCollection<T>) datastore.getDataElements();
		} else if (elementSubtype == Collection.class) {
			this.collection = (MongoCollection<T>) datastore.getCollections();
		}
		this.metadataStore = datastore.getMetadataStore();
	}
	
	public QueryExecutor<T> options(QueryOptionsMessenger options) {
		this.options = options;

		if (options != null) {
			if (options.getInclude() != null && !options.getInclude().contains("metadata")) {
				loadMetadata = true;
			}
			if (options.getExclude() != null && options.getExclude().contains("metadata")) {
				loadMetadata = false;
			}
			if (results != null) {
				if (options.getLimit() != null) {
					results.limit(options.getLimit());
				}
				if (options.getOffset() != null) {
					results.skip(options.getOffset());
				}
				if (options.getAsc() != null) {
					results.sort(Sorts.ascending(options.getAsc()));
				}
				if (options.getDesc() != null) {
					results.sort(Sorts.descending(options.getDesc()));
				}
				if (options.getInclude() != null) {
					results.projection(Projections.include(new ArrayList<>(options.getInclude())));
				}
				if (options.getExclude() != null) {
					results.projection(Projections.exclude(new ArrayList<>(options.getExclude())));
				}
			}
		}
		return this;
	}

	@Override
	public <U extends Criterion> QueryExecutor<T> find(Query<U> query) {
		totalQueryDuration = Instant.now();
		if (query != null) {
			queryDocument = postProcessQuery((QueryMongo) query, datastore);
			logger.info("Query: " + queryDocument.toJson());
		}
		return this;
	}

	@Override
	public QueryExecutor<T> xPath(String xPath) throws DatastoreException {
		if (xPath != null && !xPath.trim().equals("")) {
			List<Metadatum> metadataXPathResults;
			try {
				Duration xPathQueryDuration;
				Instant xPathQueryStart = Instant.now();

				metadataXPathResults = metadataStore.xPath(xPath);

				Instant xPathQueryEnd = Instant.now();
				xPathQueryDuration = Duration.between(xPathQueryStart, xPathQueryEnd);
				logger.info("XPath query duration: " + xPathQueryDuration.toMillis() + "ms");
			} catch (MetadataStoreException e) {
				throw new DatastoreException("Error on XPath", e);
			}

			/*Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.METADATA + "." + FieldNames.ID,
						new Document().append("$in",
								metadataXPathResults.stream().filter(metadatum -> metadatum.getId() != null)
										.map(metadatum -> new ObjectId(metadatum.getId())).collect(Collectors.toList())));*/
			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID,
						new Document().append("$in",
								metadataXPathResults.stream()/*.filter(metadatum -> metadatum.getId() != null)*/
										.map(metadatum -> new ObjectId(metadatum.getElementId()))
										.distinct().collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			queryDocument = queryDocument == null
					? retrieveXPathSatisfyElementsQuery
					: new Document().append("$and", Arrays.asList(queryDocument, retrieveXPathSatisfyElementsQuery));
		}
		return this;
	}

	@Override
	public List<T> list() throws DatastoreException {
		List<T> elements = new ArrayList<>();

		results = queryDocument == null ? collection.find() : collection.find(queryDocument);
		options(options);

		if (loadMetadata) {
			MongoCursor<T> cursor;
			try {				
				cursor = results.iterator();
			} catch (MongoQueryException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException(e.getMessage(), e);
			}

			ExecutorService executor = Executors.newFixedThreadPool(10);
			List<Future<T>> futures = new ArrayList<>();
			try {
				while (cursor.hasNext()) {
					T element = cursor.next();
					futures.add(executor.submit(() -> {
                        element.setMetadata(metadataStore.find(element.getId()));
                        logger.debug("Element " + element.getName() +" found");
                        return element;
                    }));
				}
			} finally {
				for(Future<T> future : futures) {
					try {
						elements.add(future.get());
					} catch (InterruptedException | ExecutionException e) {
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
		logger.info("Total query duration: " + Duration.between(totalQueryDuration, Instant.now()).toMillis() + "ms");
		return elements;
	}

	@Override
	public T first() throws DatastoreException {
		T element = results.first();
		options(options);
		if (element != null && loadMetadata) {
			try {
				element.setMetadata(metadataStore.find(element.getId()));
			} catch (MetadataStoreException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException(e.getMessage(), e);
			}
		}
		return element;
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
}
