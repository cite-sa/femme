package gr.cite.femme.engine.query.mongodb;

import com.mongodb.client.model.Filters;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.MetadataQueryExecutor;
import gr.cite.femme.core.query.api.MetadataQueryExecutorBuilder;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataQueryMongoExecutor<T extends Element> extends QueryMongoExecutor<T> implements MetadataQueryExecutor<T> {

	private static final Logger logger = LoggerFactory.getLogger(MetadataQueryMongoExecutor.class);

	private MetadataStore metadataStore;
	//private Map<String, List<Metadatum>> metadataXPathResults = new HashMap<>();
	private List<Metadatum> metadataXPathResults = null;

	public MetadataQueryMongoExecutor(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		super(datastore, elementSubtype);
		this.metadataStore = metadataStore;
	}

	public MetadataQueryExecutor<T> options(QueryOptionsMessenger options) {
		super.options(options);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> find(Query<? extends Criterion> query) {
		super.find(query);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPath(String xPath) throws DatastoreException, MetadataStoreException {
		xPath(new ArrayList<>(), xPath);
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPath(List<String> elementIds, String xPath) throws DatastoreException, MetadataStoreException {
		if (xPath != null && !xPath.trim().equals("")) {
			//List<Metadatum> metadataXPathResults;

			Duration xPathQueryDuration;
			Instant xPathQueryStart = Instant.now();

			this.metadataXPathResults = this.metadataStore.xPath(elementIds, xPath, isLazyMetadata());

			Instant xPathQueryEnd = Instant.now();
			xPathQueryDuration = Duration.between(xPathQueryStart, xPathQueryEnd);
			logger.info("XPath query duration: " + xPathQueryDuration.toMillis() + "ms");

			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID,
							new Document().append("$in",
									metadataXPathResults.stream()/*.filter(metadatum -> metadatum.getId() != null)*/
											.map(metadatum -> new ObjectId(metadatum.getElementId()))
											.distinct().collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			super.setQueryDocument(super.getQueryDocument() == null
					? retrieveXPathSatisfyElementsQuery
					: new Document().append("$and", Arrays.asList(
						super.getQueryDocument(),
						retrieveXPathSatisfyElementsQuery,
						new Document().append(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, new Document().append("$ne", Status.INACTIVE.getStatusCode()))
					)
			));
		}
		return this;
	}

	@Override
	public MetadataQueryExecutor<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException {
		if (xPath != null && !xPath.trim().equals("")) {

			Duration xPathQueryDuration;
			Instant xPathQueryStart = Instant.now();

			this.metadataXPathResults = this.metadataStore.xPathInMemory(xPath);

			Instant xPathQueryEnd = Instant.now();
			xPathQueryDuration = Duration.between(xPathQueryStart, xPathQueryEnd);
			logger.info("XPath query duration: " + xPathQueryDuration.toMillis() + "ms");

			Document retrieveXPathSatisfyElementsQuery = new Document()
					.append(FieldNames.ID,
							new Document().append("$in",
									this.metadataXPathResults.stream()
											.map(metadatum -> new ObjectId(metadatum.getElementId()))
											.distinct().collect(Collectors.toList())));
									//this.metadataXPathResults.keySet().stream().map(ObjectId::new).collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			super.setQueryDocument(super.getQueryDocument() == null
					? retrieveXPathSatisfyElementsQuery
					: new Document().append("$and", Arrays.asList(
						super.getQueryDocument(),
						retrieveXPathSatisfyElementsQuery
					)
				));
		}

		return this;
	}

	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		List<T> elements;
		elements = super.list();
		if (!isLazyMetadata()) {
			try {
				elements = elements.stream().map(element -> {
					if (this.metadataXPathResults != null) {
						List<Metadatum> xPathResult = this.metadataXPathResults.stream()
								.filter(metadatum -> metadatum.getElementId().equals(element.getId()))
								.map(metadatum -> {
									if (metadatum.getValue() == null) {
										try {
											return this.metadataStore.get(metadatum, false);
										} catch (MetadataStoreException e) {
											throw new RuntimeException(e.getMessage(), e);
										}
									} else {
										Metadatum originalMetadatum;
										try {
											originalMetadatum = this.metadataStore.get(metadatum, true);
										} catch (MetadataStoreException e) {
											throw new RuntimeException(e.getMessage(), e);
										}

										originalMetadatum.setValue(metadatum.getValue());
										return originalMetadatum;
									}
								}).collect(Collectors.toList());

						if (this.metadataXPathResults.size() > 0) {
							element.setMetadata(xPathResult);
						}
					} else {
						try {
							element.setMetadata(this.metadataStore.find(element.getId(), isLazyMetadata()));
						} catch (MetadataStoreException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return element;
				}).collect(Collectors.toList());
			} catch (RuntimeException e) {
				throw new MetadataStoreException(e.getMessage(), e);
			}
		}

		return elements;
	}

	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		T element = super.first();

		if (!isLazyMetadata()) {
			if (this.metadataXPathResults != null) {
				List<Metadatum> xPathResult = this.metadataXPathResults.stream()
						.filter(metadatum -> metadatum.getElementId().equals(element.getId()))
						.map(metadatum -> {
							if (metadatum.getValue() == null) {
								try {
									return this.metadataStore.get(metadatum, false);
								} catch (MetadataStoreException e) {
									throw new RuntimeException(e.getMessage(), e);
								}
							} else {
								/*//Metadatum metadatumInfo = this.metadataStore.get(metadatum, true);
								Metadatum metadatumResponse = new Metadatum();
								metadatumResponse.setId(metadatum.getId());
								metadatumResponse.setValue(metadatum.getValue());
								return metadatumResponse;*/

								Metadatum originalMetadatum;
								try {
									originalMetadatum = this.metadataStore.get(metadatum, true);
								} catch (MetadataStoreException e) {
									throw new RuntimeException(e.getMessage(), e);
								}

								originalMetadatum.setValue(metadatum.getValue());
								return originalMetadatum;
							}
						}).collect(Collectors.toList());

				if (this.metadataXPathResults.size() > 0) {
					element.setMetadata(xPathResult);
				}
			} else {
				element.setMetadata(this.metadataStore.find(element.getId(), isLazyMetadata()));
			}
		}

		//element.setMetadata(this.metadataStore.find(element.getId(), super.isLazyMetadata()));
		return element;
	}

	public static <U extends Element>QueryExecutionBuilder<U> builder(MongoDatastore datastore, MetadataStore metadataStore, Class<U> elementSubtype) {
		return new QueryExecutionBuilder<>(datastore, metadataStore, elementSubtype);
	}

	public static class QueryExecutionBuilder<T extends Element> implements MetadataQueryExecutorBuilder<T>{
		private MetadataQueryMongoExecutor<T> queryExecutor;

		private Query<? extends Criterion> query;
		private QueryOptionsMessenger options;
		private String xPath;

		private QueryExecutionBuilder(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
			queryExecutor = new MetadataQueryMongoExecutor<>(datastore, metadataStore, elementSubtype);
		}

		public MetadataQueryExecutorBuilder<T> find(Query<? extends Criterion> query) {
			this.query = query;
			//queryExecutor.find(query);
			return this;
		}

		public MetadataQueryExecutorBuilder<T> options(QueryOptionsMessenger options) {
			this.options = options;
			//queryExecutor.options(options);
			return this;
		}

		public MetadataQueryExecutorBuilder<T> xPath(String xPath) throws MetadataStoreException, DatastoreException {
			this.xPath = xPath;
			//queryExecutor.xPath(xPath);
			return this;
		}

		public MetadataQueryExecutorBuilder<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException {
			queryExecutor.xPathInMemory(xPath);
			return this;
		}

		public MetadataQueryExecutor<T> build() throws MetadataStoreException, DatastoreException {
			queryExecutor.find(query);
			queryExecutor.options(options);

			if (query != null || xPath != null) {
				queryExecutor.xPath(queryExecutor.list().stream().map(Element::getId).collect(Collectors.toList()), xPath);
			}

			return queryExecutor;
		}
	}

}
