package gr.cite.femme.engine;

import gr.cite.femme.engine.datastore.mongodb.MongoDatastore;
import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.api.Datastore;
import gr.cite.femme.api.MetadataStore;
import gr.cite.femme.core.exceptions.DatastoreException;
import gr.cite.femme.core.exceptions.FemmeException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.exceptions.MetadataStoreException;
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
import gr.cite.femme.engine.query.mongodb.QueryMongo;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public String insert(Element element) throws FemmeException {
		if (element.getId() == null) {
			element.setId(this.datastore.generateElementId());
		}
		element.getMetadata().forEach(metadatum -> {
			/*if (metadatum.getId() != null) {
				metadatum.setId(this.metadataStore.generateMetadatumId());
			}*/
			metadatum.setElementId(element.getId());
		});

		String id;
		try {
			id = exists(element);
		} catch (DatastoreException e) {
			String errorMessage = element.getClass().getSimpleName() + " " + element.getEndpoint() + "-" + element.getName() + " existence check failed";
			logger.error(errorMessage, e);
			throw new FemmeException(errorMessage, e);
		}

		if (id != null) {
			element.setId(id);
			try {
				update(element);
			} catch (DatastoreException e) {
				logger.error(element.getClass().getSimpleName() + " " + element.getId() + " update failed", e);
				throw new FemmeException(element.getClass().getSimpleName() + " " + element.getId() + " update failed", e);
			}
		} else {
			// Insert Metadata
			for (Metadatum metadatum : element.getMetadata()) {
				try {
					this.metadataStore.insert(metadatum);
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
					//throw new FemmeException(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);
				}
			}

			// Insert Element
			if (element.getSystemicMetadata() == null) {
				element.setSystemicMetadata(new SystemicMetadata());
			}
			element.getSystemicMetadata().setStatus(Status.ACTIVE);

			try {
				this.datastore.insert(element);
			} catch (DatastoreException e) {
				logger.error(element.getClass().getSimpleName() + " " + element.getId() + " insertion failed", e);
				throw new FemmeException(element.getClass().getSimpleName() + " " + element.getId() + " insertion failed", e);
			}

			if (element instanceof Collection) {
				// Insert Collection's DataElements
				Collection collection = (Collection) element;
				for (DataElement dataElement: collection.getDataElements()) {
					dataElement.addCollection(collection);
					if (dataElement.getId() == null) {
						dataElement.setId(Femme.generateId());
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
		}

		return element.getId();
	}

	private String exists(Element element) throws DatastoreException {
		Element existingElement = this.datastore
				.find(QueryMongo.query()
						.addCriterion(CriterionBuilderMongo.root().and(Arrays.asList(
								CriterionBuilderMongo.root().eq(FieldNames.NAME, element.getName()).end(),
								CriterionBuilderMongo.root().eq(FieldNames.ENDPOINT, element.getEndpoint()).end()
						)).end()), element.getClass(), this.metadataStore)
				.options(QueryOptionsMessenger.builder().include(FieldNames.ID).build()).first();
		return existingElement != null ? existingElement.getId() : null;
	}

	private void update(Element element) throws DatastoreException {
		// TODO implement update
		// Find if exists
		/*Element existingElement = this.datastore
				.find(QueryMongo.query()
						.addCriterion(CriterionBuilderMongo.root().and(Arrays.asList(
								CriterionBuilderMongo.root().eq(FieldNames.NAME, element.getName()).end(),
								CriterionBuilderMongo.root().eq(FieldNames.ENDPOINT, element.getEndpoint()).end()
						)).end()), element.getClass(), this.metadataStore)
				.first();*/

		/*if (existingElement != null) {*/
			// Get existing
			Element elementToBeUpdated = this.datastore.get(element.getId(), element.getClass(), this.metadataStore, null);

			List<String> failedMetadata = new ArrayList<>();
			elementToBeUpdated.setMetadata(new ArrayList<>());
			for (Metadatum metadatum: element.getMetadata()) {
				metadatum.setElementId(elementToBeUpdated.getId());
				try {
					this.metadataStore.update(metadatum);
					elementToBeUpdated.getMetadata().add(metadatum);
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
					failedMetadata.add(metadatum.getId());
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);
				}
			}
			//List<String> existingMetdataChecksums = elementToBeUpdated.getMetadata().stream().map(Metadatum::getChecksum).collect(Collectors.toList());

			//List<Metadatum> existingMetadata = this.metadataStore.find(element.getId());

			// Filter new metadata
			/*elementToBeUpdated.setMetadata(element.getMetadata().stream()
					.filter(metadatum -> metadatum.getSystemicMetadata().getStatus() == Status.ACTIVE && !existingMetdataChecksums.contains(metadatum.getChecksum()))
					.map(metadatum -> {
						metadatum.setId(Femme.generateId());
						metadatum.setElementId(elementToBeUpdated.getId());
						return metadatum;
					}).collect(Collectors.toList()));

			this.datastore.update(elementToBeUpdated);

			List<String> failedMetadata = new ArrayList<>();
			for (Metadatum metadatum : element.getMetadata()) {
				try {
					this.metadataStore.insert(metadatum);
				} catch (MetadataStoreException e) {
					logger.error(element.getClass().getSimpleName() + " " + element.getId() + " metadatum insertion failed", e);
					failedMetadata.add(metadatum.getId());
				} catch (MetadataIndexException e) {
					logger.warn(element.getClass().getSimpleName() + " " + element.getId() + " metadatum indexing failed", e);
				}
			}*/

			failedMetadata.forEach(id -> elementToBeUpdated.getMetadata().removeIf(metadatum -> metadatum.getId().equals(id)));

			this.datastore.update(elementToBeUpdated);
			/*}*/
	}

	public <T extends Element> void deactivateElement(String id, Class<T> elementSubType) throws FemmeException {
		//Element modifyStatusElement;
		try {
			//modifyStatusElement = this.datastore.get(id, elementSubType, this.metadataStore, QueryOptionsMessenger.builder().exclude(FieldNames.METADATA).build());
			//modifyStatusElement.getSystemicMetadata().setStatus(Status.INACTIVE);

			Map<String, Object> fieldsAndValues = new HashMap<>();
			fieldsAndValues.put(FieldNames.SYSTEMIC_METADATA + "." + FieldNames.STATUS, Status.INACTIVE.getStatusCode());
			//this.datastore.update(modifyStatusElement);
			Element updatedElement = this.datastore.update(id, fieldsAndValues, elementSubType);

			for (Metadatum metadatum: updatedElement.getMetadata()) {
				this.metadataStore.unIndex(metadatum.getId());
			}
		} catch (DatastoreException | MetadataIndexException e) {
			throw new FemmeException("Deactivate " + elementSubType.getSimpleName() + " " + id + " failed", e);
		}
	}

	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeException {
		Collection collection;
		try {
			collection = this.datastore.get(collectionId, Collection.class, this.metadataStore, null);
		} catch (DatastoreException e) {
			throw new FemmeException("Collection " + collectionId + " retrieval failed", e);
		}

		if (collection != null) {
			dataElement.setCollections(Collections.singletonList(collection));
			return insert(dataElement);
		}

		return null;
	}

	public String addToCollection(DataElement dataElement, Query<? extends Criterion> query) throws FemmeException {
		List<Collection> collections;
		try {
			collections = this.datastore.find(query, Collection.class, this.metadataStore).list();
		} catch (DatastoreException e) {
			throw new FemmeException("Collections " + query.toString() + " retrieval failed", e);
		}

		if (collections != null && collections.size() > 0) {
			dataElement.setCollections(collections);
			return insert(dataElement);
		}

		return null;
	}

	public <T extends Element> T find(String id, Class<T> elementSubType) throws DatastoreException {
		return this.datastore.get(id, elementSubType, this.metadataStore, null);
	}

	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return this.datastore.find(query, elementSubtype, this.metadataStore);
	}

	public <T extends Element> long count(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return this.datastore.count(query, elementSubtype);
	}

	public void reIndex() throws FemmeException {
		try {
			this.metadataStore.reIndexAll();
		} catch (MetadataIndexException | MetadataStoreException e) {
			throw new FemmeException("Reindexing failed", e);
		}
	}

	private static String generateId() {
		return new ObjectId().toString();
	}

	public static void main(String[] args) throws DatastoreException {
		Femme femme = new Femme();
		QueryExecutor<DataElement> queryExecutor = femme.find(QueryMongo.query().addCriterion(CriterionBuilderMongo.root().inAnyCollection(
				Arrays.asList(CriterionBuilderMongo.root().eq(FieldNames.ID, new ObjectId("58e51c549a319213d7cca531")).end()
				)).end()), DataElement.class).options(QueryOptionsMessenger.builder().include(Sets.newHashSet("id")).build());

		List<DataElement> existingDataElements = queryExecutor.list();

		System.out.println(existingDataElements);

	}
}
