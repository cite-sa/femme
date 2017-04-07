package gr.cite.femme;

import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.api.MetadataStore;
import gr.cite.femme.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.exceptions.DatastoreException;
import gr.cite.femme.exceptions.FemmeException;
import gr.cite.femme.exceptions.MetadataIndexException;
import gr.cite.femme.exceptions.MetadataStoreException;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.query.api.Criterion;
import gr.cite.femme.query.api.Query;
import gr.cite.femme.query.api.QueryExecutor;
import gr.cite.femme.query.mongodb.CriterionBuilderMongo;
import gr.cite.femme.query.mongodb.QueryMongo;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Femme {

	private static final Logger logger = LoggerFactory.getLogger(Femme.class);

	private Datastore datastore;
	private MetadataStore metadataStore;

	public Femme(Datastore datastore, MetadataStore metadataStore) {
		this.datastore = datastore;
		this.metadataStore = metadataStore;
	}

	public String insert(Element element) throws FemmeException {

		if (element.getId() != null) {
			element.setId(this.datastore.generateElementId());
		}
		element.getMetadata().forEach(metadatum -> {
			if (metadatum.getId() != null) {
				metadatum.setId(this.metadataStore.generateMetadatumId());
			}
			metadatum.setElementId(element.getId());
			/*try {
				metadatum.setChecksum(ChecksumGeneratorUtils.generateMD5(metadatum.getValue()));
			} catch (HashGenerationException e) {
				logger.error(e.getMessage(), e);
			}*/
			//metadatum.getSystemicMetadata().setStatus(Status.PENDING);
		});

		/*if (this.datastore.get(element.getId(), element.getClass(), this.metadataStore) != null) {

		}*/

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
			// Insert Element
			try {
				this.datastore.insert(element);
			} catch (DatastoreException e) {
				logger.error(element.getClass().getSimpleName() + " " + element.getId() + " insertion failed", e);
				throw new FemmeException(element.getClass().getSimpleName() + " " + element.getId() + " insertion failed", e);
			}

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
				.first();
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
			Element elementToBeUpdated = this.datastore.get(element.getId(), element.getClass(), this.metadataStore);

			List<String> failedMetadata = new ArrayList<>();
			for (Metadatum metadatum: element.getMetadata()) {
				metadatum.setElementId(elementToBeUpdated.getId());
				try {
					this.metadataStore.update(metadatum);
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

	public String addToCollection(DataElement dataElement, String collectionId) throws FemmeException {
		Collection collection;
		try {
			collection = this.datastore.get(collectionId, Collection.class, this.metadataStore);
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
		return this.datastore.get(id, elementSubType, this.metadataStore);
	}

	public <T extends Element> QueryExecutor<T> find(Query<? extends Criterion> query, Class<T> elementSubtype) {
		return this.datastore.find(query, elementSubtype, this.metadataStore);
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
}
