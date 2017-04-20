package gr.cite.femme.engine;

import gr.cite.femme.core.query.api.MetadataQueryExecutor;
import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.FemmeException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
import gr.cite.femme.engine.metadata.xpath.mongodb.evaluation.MongoQuery;
import gr.cite.femme.engine.metadatastore.mongodb.MongoMetadataStore;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.Status;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.query.api.Criterion;
import gr.cite.femme.core.query.api.Query;
import gr.cite.femme.core.query.api.QueryExecutor;
import gr.cite.femme.core.query.api.QueryOptionsMessenger;
import gr.cite.femme.engine.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.engine.query.mongodb.CriterionMongo;
import gr.cite.femme.engine.query.mongodb.QueryMongo;
import gr.cite.femme.engine.query.mongodb.QueryOptionsBuilderMongo;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.crypto.Data;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Femme {
	private static final Logger logger = LoggerFactory.getLogger(Femme.class);

	private Datastore datastore;
	private MetadataStore metadataStore;

	public Femme() {
		this.datastore = new MongoDatastore();
		this.metadataStore = new MongoMetadataStore();
	}

	@Inject
	public Femme(Datastore datastore, MetadataStore metadataStore) {
		this.datastore = datastore;
		this.metadataStore = metadataStore;
	}

	public String insert(Element element) throws DatastoreException, MetadataStoreException {
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
		}

		return element.getId();
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

	public Element update(Element element) throws DatastoreException, MetadataStoreException {
		// TODO implement update
		Element updatedElement = null;
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
					this.metadataStore.deactivate(obsoleteMetadatum.getId());
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

	public void deactivateElement(String id, Class<? extends Element> elementSubType) throws FemmeException {
		try {
			this.datastore.deactivate(id, elementSubType);
			this.metadataStore.deactivateAll(id);
		} catch (DatastoreException | MetadataIndexException | MetadataStoreException e) {
			throw new FemmeException("Deactivate " + elementSubType.getSimpleName() + " " + id + " failed", e);
		}
	}

	public String addToCollection(DataElement dataElement, String collectionId) throws DatastoreException, MetadataStoreException {
		Collection collection;

		collection = this.datastore.get(collectionId, Collection.class, null);

		if (collection != null) {
			dataElement.setCollections(Collections.singletonList(collection));
			return insert(dataElement);
		}

		return null;
	}

	public String addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws DatastoreException, MetadataStoreException {
		List<Collection> collections;
		collections = this.datastore.find(query, Collection.class).list();

		if (collections != null && collections.size() > 0) {
			dataElement.setCollections(collections);
			return insert(dataElement);
		}

		return null;
	}

	public <T extends Element> T get(String id, Class<T> elementSubType) throws DatastoreException, MetadataStoreException {
		try {
			return find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().eq(FieldNames.ID, this.datastore.generateId(id)).end()), elementSubType).first();
		} catch (IllegalArgumentException e) {
			throw new DatastoreException("Invalid " + elementSubType.getSimpleName() + " id: [" + id + "]");
		}
	}

	public <T extends Element> MetadataQueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubType) {
		return new QueryOptionsBuilderMongo<T>().query(this.datastore, this.metadataStore, elementSubType).find(query);
	}

	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return this.datastore.count(query, elementSubtype);
	}

	public void reIndex() throws MetadataStoreException, MetadataIndexException {
		this.metadataStore.reIndexAll();
	}
}
