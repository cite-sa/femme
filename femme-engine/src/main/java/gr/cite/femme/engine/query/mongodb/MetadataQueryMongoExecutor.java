package gr.cite.femme.engine.query.mongodb;

import com.mongodb.client.model.Filters;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.MetadataQueryExecutor;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataQueryMongoExecutor<T extends Element> extends QueryMongoExecutor<T> implements MetadataQueryExecutor<T> {

	private static final Logger logger = LoggerFactory.getLogger(MetadataQueryMongoExecutor.class);

	private MetadataStore metadataStore;

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
									metadataXPathResults.stream()/*.filter(metadatum -> metadatum.getId() != null)*/
											.map(metadatum -> new ObjectId(metadatum.getElementId()))
											.distinct().collect(Collectors.toList())));
			logger.debug(retrieveXPathSatisfyElementsQuery.toString());

			super.setQueryDocument(super.getQueryDocument() == null
					? retrieveXPathSatisfyElementsQuery
					: new Document().append("$and", Arrays.asList(
					super.getQueryDocument(),
					retrieveXPathSatisfyElementsQuery,
					Filters.ne(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode())
			)));
		}
		return this;
	}

	@Override
	public List<T> list() throws DatastoreException, MetadataStoreException {
		try {
			return super.list().parallelStream().map(element -> {
				try {
					element.setMetadata(this.metadataStore.find(element.getId(), super.isLazyMetadata()));
					return element;
				} catch (MetadataStoreException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}).collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}

	@Override
	public T first() throws DatastoreException, MetadataStoreException {
		T element = super.first();
		element.setMetadata(this.metadataStore.find(element.getId(), super.isLazyMetadata()));
		return element;
	}

}
