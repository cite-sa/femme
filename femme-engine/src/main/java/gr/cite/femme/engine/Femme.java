package gr.cite.femme.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import gr.cite.commons.pipeline.ProcessingPipeline;
import gr.cite.commons.pipeline.ProcessingPipelineException;
import gr.cite.commons.pipeline.config.PipelineConfiguration;
import gr.cite.femme.core.query.execution.MetadataQueryExecutorBuilder;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.datastores.Datastore;
import gr.cite.femme.core.datastores.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.FemmeException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.query.construction.Criterion;
import gr.cite.femme.core.query.construction.Query;
import gr.cite.femme.core.dto.QueryOptionsMessenger;
import gr.cite.femme.engine.query.construction.mongodb.CriterionBuilderMongo;
import gr.cite.femme.engine.query.execution.mongodb.QueryExecutorFactory;
import gr.cite.femme.engine.query.construction.mongodb.QueryMongo;
import gr.cite.femme.fulltext.client.FulltextIndexClientAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Femme {
	private static final Logger logger = LoggerFactory.getLogger(Femme.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String PIPELINE_CONFIG_FILE = "pipeline-config.json";
	private static final String FULLTEXT_PIPELINE_CONFIG = "fulltext";
	private static final String GEO_PIPELINE_CONFIG = "geo";

	private Datastore datastore;
	private MetadataStore metadataStore;
	private FulltextIndexClientAPI fulltextClient;

	private Map<String, PipelineConfiguration> pipelineConfiguration;

	//@Inject
	public Femme(Datastore datastore, MetadataStore metadataStore) throws IOException {
		this.datastore = datastore;
		this.metadataStore = metadataStore;

		String config = Resources.toString(Resources.getResource(Femme.PIPELINE_CONFIG_FILE), Charsets.UTF_8);
		if (!config.trim().isEmpty()) {
			this.pipelineConfiguration = mapper.readValue(config, new TypeReference<Map<String, PipelineConfiguration>>() {});
		}
	}

	@Inject
	public Femme(Datastore datastore, MetadataStore metadataStore, FulltextIndexClientAPI fulltextClient) throws IOException {
		this.datastore = datastore;
		this.metadataStore = metadataStore;
		this.fulltextClient = fulltextClient;

		String config = Resources.toString(Resources.getResource(Femme.PIPELINE_CONFIG_FILE), Charsets.UTF_8);
		if (!config.trim().isEmpty()) {
			this.pipelineConfiguration = mapper.readValue(config, new TypeReference<Map<String, PipelineConfiguration>>() {});
		}
	}

	public String insert(Element element) throws DatastoreException, MetadataStoreException, FemmeException {
		if (element.getId() == null) {
			element.setId(this.datastore.generateId());
		}
		element.getMetadata().forEach(metadatum -> metadatum.setElementId(element.getId()));

		String id = exists(element);
		if (id != null) {
			element.setId(id);
			update(element);
		} else {
			// Insert Element
			if (element.getSystemicMetadata() == null) {
				element.setSystemicMetadata(new SystemicMetadata());
			}
			element.getSystemicMetadata().setStatus(Status.ACTIVE);

			this.datastore.insert(element);

			if (element instanceof Collection) {
				// Insert Collection's DataElements
				Collection collection = (Collection) element;
				for (DataElement dataElement: collection.getDataElements()) {
					dataElement.addCollection(collection);
					if (dataElement.getId() == null) {
						dataElement.setId(this.datastore.generateId());
					}
					insert(dataElement);
				}
				// TODO check how subDataElements are stored in MongoDB
			} else if (element instanceof DataElement) {
				// Insert DataElement's DataElements
				DataElement dataElement = (DataElement) element;
				for (DataElement subDataElement: dataElement.getDataElements()) {
					insert(subDataElement);
				}
			}

			// Insert Metadata
			for (Metadatum metadatum : element.getMetadata()) {
				try {
					this.metadataStore.insert(metadatum);
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);
				}
			}

			if (this.fulltextClient != null) {
				insertInFulltextIndex(element);
			}
		}

		return element.getId();
	}

	public String insert(Metadatum metadatum) throws FemmeException {
		try {
			this.metadataStore.insert(metadatum);
			return metadatum.getId();
		} catch (MetadataStoreException | MetadataIndexException e) {
			throw new FemmeException("Metadatum insertion failed", e);
		}
	}

	private String exists(Element element) throws DatastoreException, MetadataStoreException {
		Element existingElement = this.datastore
				.find(QueryMongo.query()
						.addCriterion(CriterionBuilderMongo.root().and(Arrays.asList(
								CriterionBuilderMongo.root().eq(FieldNames.NAME, element.getName()).end(),
								CriterionBuilderMongo.root().eq(FieldNames.ENDPOINT, element.getEndpoint()).end()
						)).end()), element.getClass())
				.options(QueryOptionsMessenger.builder().include(FieldNames.ID).build()).first();
		return existingElement != null ? existingElement.getId() : null;
	}

	public <T extends Element> T update(Element element) throws DatastoreException, MetadataStoreException {
		// TODO implement update
		T updatedElement = null;
		if (element.getId() != null) {
			/*try {
				updateElement = this.datastore.get(element.getId(), element.getClass(), this.metadataStore, null);
			} catch (DatastoreException e) {
				throw new FemmeException(element.getClass().getSimpleName() + " " + element.getId() + " update failed", e);
			}*/

			updatedElement = this.datastore.update(element);

			List<Metadatum> existingMetadata;
			existingMetadata = this.metadataStore.find(element.getId());

			for (Metadatum metadatum : element.getMetadata()) {
				metadatum.setElementId(element.getId());
				try {
					Metadatum updatedMetadatum = this.metadataStore.update(metadatum);

					if (updatedMetadatum != null) {
						existingMetadata.removeIf(existing -> existing.getId().equals(updatedMetadatum.getId()));
					}
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);

				}
			}

			for (Metadatum obsoleteMetadatum : existingMetadata) {
				try {
					this.metadataStore.softDelete(obsoleteMetadatum.getId());
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);
				}
			}
		}

		return updatedElement;
	}

	public Metadatum update(Metadatum metadatum	) throws FemmeException {
		try {
			return this.metadataStore.update(metadatum);
		} catch (MetadataStoreException | MetadataIndexException e) {
			throw new FemmeException("Metadatum " + metadatum.getId() + " update failed", e);
		}
	}

	public void softDeleteElement(String id, Class<? extends Element> elementSubType) throws FemmeException {
		try {
			this.metadataStore.softDeleteAll(id);
			this.datastore.softDelete(id, elementSubType);
		} catch (DatastoreException | MetadataIndexException | MetadataStoreException e) {
			throw new FemmeException("Soft delete " + elementSubType.getSimpleName() + " " + id + " failed", e);
		}
	}

	public void softDeleteMetadatum(String id) throws FemmeException {
		try {
			this.metadataStore.softDelete(id);
		} catch (MetadataIndexException | MetadataStoreException e) {
			throw new FemmeException("Soft delete metadatum " + id + " failed", e);
		}
	}

	public String addToCollection(DataElement dataElement, String collectionId) throws DatastoreException, MetadataStoreException, IllegalArgumentException, FemmeException {
		Collection collection = this.datastore.get(collectionId, Collection.class);
		if (collection == null) {
			throw new DatastoreException("Collection doesn't exist [" + collectionId + "]");
		}

		dataElement.setCollections(Collections.singletonList(collection));
		return insert(dataElement);
	}

	public String addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException, MetadataStoreException, FemmeException {
		List<Collection> collections;
		collections = this.datastore.find(query, Collection.class).list();

		if (collections != null && collections.size() > 0) {
			dataElement.setCollections(collections);
			return insert(dataElement);
		}

		return null;
	}

	public <T extends Element> T get(String id, Class<T> elementSubType) throws DatastoreException, MetadataStoreException {
		return get(id, elementSubType, false);
	}

	public <T extends Element> T get(String id, Class<T> elementSubType, boolean loadInactiveMetadata) throws DatastoreException, MetadataStoreException {
		try {
			return query(elementSubType).find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, this.datastore.generateId(id)).end()))
					.options(QueryOptionsMessenger.builder().loadInactiveMetadata(loadInactiveMetadata).build())
					.execute().first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException("Invalid " + elementSubType.getSimpleName() + " id: [" + id + "]");
		}
	}

	public <T extends Element> T get(String id, String xPath, Class<T> elementSubType) throws DatastoreException, MetadataStoreException {
		try {
			return query(elementSubType).find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, this.datastore.generateId(id)).end())).xPath(xPath).execute().first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException("Invalid " + elementSubType.getSimpleName() + " id: [" + id + "]");
		}
	}

	/*public <T extends Element> MetadataQueryExecutor<T> find(Query<? extends Criterion> getQueryExecutor, Class<T> elementSubType) {
		return new QueryOptionsBuilderMongo<T>().getQueryExecutor(this.datastore, this.metadataStore, elementSubType).find(getQueryExecutor);
	}*/
	/*public <T extends Element> MetadataQueryExecutor<T> find(Query<? extends Criterion> getQueryExecutor, Class<T> elementSubType) {
		return new QueryOptionsBuilderMongo<T>().getQueryExecutor(this.datastore, this.metadataStore, elementSubType).find(getQueryExecutor);
	}*/

	public <T extends Element> MetadataQueryExecutorBuilder<T> query(Class<T> elementSubType) {
		return QueryExecutorFactory.getQueryExecutor(this.datastore, this.metadataStore, elementSubType);
	}

	/*public <T extends Element> MetadataQueryExecutorBuilder<T> find(Query<? extends Criterion> getQueryExecutor, Class<T> elementSubType) {
		return new QueryExecutorFactory<T>().getQueryExecutor(this.datastore, this.metadataStore, elementSubType).find(getQueryExecutor);
	}*/

	/*public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return this.datastore.count(query, elementSubtype);
	}*/

	public void reIndex() throws MetadataStoreException, MetadataIndexException {
		this.metadataStore.reIndexAll();
	}

	private void insertInFulltextIndex(Element element) throws FemmeException {
		if (this.pipelineConfiguration != null) {
			ProcessingPipeline pipeline;
			pipeline = new ProcessingPipeline(this.pipelineConfiguration.get(Femme.FULLTEXT_PIPELINE_CONFIG));

			for (Metadatum metadatum : element.getMetadata()) {
				try {
					this.fulltextClient.insert(metadatum.getElementId(), metadatum.getId(), pipeline.process(metadatum.getValue(), metadatum.getContentType().toLowerCase().split("/")[1]));
				} catch (ProcessingPipelineException e) {
					throw new FemmeException(e.getMessage(), e);
				}
			}
		}
	}

	private void deleteFromFulltextIndexByElementId(String elementId) throws FemmeException {
			this.fulltextClient.deleteByElementId(elementId);
	}

	private void deleteFromFulltextIndexByMetadatumId(String metadatumId) throws FemmeException {
		// TODO implement
	}

	private void search() {

	}

}
