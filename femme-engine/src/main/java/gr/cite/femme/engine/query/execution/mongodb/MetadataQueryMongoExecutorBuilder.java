package gr.cite.femme.engine.query.execution.mongodb;/*if (existing != null) {
				metadatum.setId(existing.getId());
			}*/

import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.execution.MetadataQueryExecutor;
import gr.cite.femme.core.query.execution.MetadataQueryExecutorBuilder;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataQueryMongoExecutorBuilder<T extends Element> implements MetadataQueryExecutorBuilder<T> {
	private MongoDatastore datastore;
	private MetadataStore metadataStore;
	private Class<T> elementSubtype;

	MetadataQueryMongoExecutorBuilder(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
		this.datastore = datastore;
		this.metadataStore = metadataStore;
		this.elementSubtype = elementSubtype;
	}

	public FindQueryExecutorBuilder<T> find(Query<? extends Criterion> query) {
		return new FindQueryMongoExecutorBuilder<>(this.datastore, this.metadataStore, this.elementSubtype).find(query);
	}

	public CountQueryMongoExecutionBuilder<T> count(Query<? extends Criterion> query) {
		return new CountQueryMongoExecutionBuilder<>(this.datastore, this.metadataStore, this.elementSubtype).count(query);
	}


	public static class FindQueryMongoExecutorBuilder<T extends Element> implements FindQueryExecutorBuilder<T> {
		private MongoDatastore datastore;
		private Class<T> elementSubtype;

		private MetadataQueryMongoExecutor<T> queryExecutor;

		private Query<? extends Criterion> query;
		private QueryOptionsMessenger options;
		private String xPath;

		private FindQueryMongoExecutorBuilder(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
			this.datastore = datastore;
			this.elementSubtype = elementSubtype;
			this.queryExecutor = new MetadataQueryMongoExecutor<>(datastore, metadataStore, elementSubtype);
		}

		public FindQueryExecutorBuilder<T> find(Query<? extends Criterion> query) {
			this.query = query;
			return this;
		}

		public FindQueryMongoExecutorBuilder<T> options(QueryOptionsMessenger options) {
			this.options = options;
			return this;
		}

		public FindQueryMongoExecutorBuilder<T> xPath(String xPath) throws MetadataStoreException, DatastoreException {
			this.xPath = xPath;
			return this;
		}

		public FindQueryMongoExecutorBuilder<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException {
			queryExecutor.xPathInMemory(xPath);
			return this;
		}

		public MetadataQueryExecutor<T> execute() throws MetadataStoreException, DatastoreException {

			if (this.xPath != null) {
				QueryMongoExecutor<T> preFilteringQueryExecutor = new QueryMongoExecutor<>(this.datastore, this.elementSubtype);
				List<T> preFilteringIds = new ArrayList<>();
				if (this.query != null) {
					preFilteringIds = preFilteringQueryExecutor.find(this.query).options(QueryOptionsMessenger.builder().include(FieldNames.ID).build()).list();
				}

				if (this.query == null || preFilteringIds.size() > 0) {
					this.queryExecutor.xPath(preFilteringIds.stream().map(Element::getId).collect(Collectors.toList()), xPath);
				} else {
					this.options = QueryOptionsMessenger.builder().limit(0).build();
				}
				this.query = null;
			}

			this.queryExecutor.find(this.query);
			this.queryExecutor.options(this.options != null ? this.options : QueryOptionsMessenger.builder().build());

			return this.queryExecutor;
		}
	}

	public static class CountQueryMongoExecutionBuilder<T extends Element> implements CountQueryExecutorBuilder<T> {
		private MongoDatastore datastore;
		private Class<T> elementSubtype;

		private MetadataQueryMongoExecutor<T> queryExecutor;

		private Query<? extends Criterion> query;
		private String xPath;

		private CountQueryMongoExecutionBuilder(MongoDatastore datastore, MetadataStore metadataStore, Class<T> elementSubtype) {
			this.datastore = datastore;
			this.elementSubtype = elementSubtype;
			this.queryExecutor = new MetadataQueryMongoExecutor<>(datastore, metadataStore, elementSubtype);
		}

		public CountQueryMongoExecutionBuilder<T> count(Query<? extends Criterion> query) {
			this.query = query;
			return this;
		}

		public CountQueryMongoExecutionBuilder<T> xPath(String xPath) throws MetadataStoreException, DatastoreException {
			this.xPath = xPath;
			return this;
		}

		public CountQueryMongoExecutionBuilder<T> xPathInMemory(String xPath) throws DatastoreException, MetadataStoreException {
			queryExecutor.xPathInMemory(xPath);
			return this;
		}

		public long execute() throws MetadataStoreException, DatastoreException {

			if (this.xPath != null) {
				QueryMongoExecutor<T> preFilteringQueryExecutor = new QueryMongoExecutor<>(this.datastore, this.elementSubtype);
				List<T> preFilteringIds = new ArrayList<>();
				if (this.query != null) {
					preFilteringIds = preFilteringQueryExecutor.find(this.query).options(QueryOptionsMessenger.builder().include(FieldNames.ID).build()).list();
				}

				if (preFilteringIds.size() > 0) {
					this.queryExecutor.xPath(preFilteringIds.stream().map(Element::getId).collect(Collectors.toList()), xPath);
				} else {
					return 0;
				}
				this.query = null;
			}

			return this.queryExecutor.count(this.query);
		}
	}
}
