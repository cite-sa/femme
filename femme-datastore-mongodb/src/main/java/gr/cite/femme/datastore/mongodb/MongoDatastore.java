package gr.cite.femme.datastore.mongodb;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoGridFSException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.datastore.api.Datastore;
import gr.cite.femme.datastore.exceptions.DatastoreException;
import gr.cite.femme.datastore.exceptions.IllegalElementSubtype;
import gr.cite.femme.datastore.mongodb.gridfs.MetadatumGridFS;
import gr.cite.femme.datastore.mongodb.utils.Documentizer;
import gr.cite.femme.datastore.mongodb.utils.ElementFields;
import gr.cite.femme.query.ICriteria;
import gr.cite.femme.query.IQuery;
import gr.cite.femme.query.IQueryOptions;
import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Query;
import gr.cite.femme.query.mongodb.QueryOptions;

public class MongoDatastore implements Datastore<Criteria, Query>  {
	private static final Logger logger = LoggerFactory.getLogger(MongoDatastore.class);

	MongoDatastoreClient mongoClient;
	MongoCollection<Collection> collections;
	MongoCollection<DataElement> dataElements;
	MetadatumGridFS metadatumGridfs;

	public MongoDatastore() {
		mongoClient = new MongoDatastoreClient();
		collections = mongoClient.getCollections();
		dataElements = mongoClient.getDataElements();
		this.metadatumGridfs = mongoClient.getMetadatumGridFS();
	}

	public MongoDatastore(MongoDatabase db) {
		mongoClient = new MongoDatastoreClient(db);
		collections = mongoClient.getCollections();
		dataElements = mongoClient.getDataElements();
		this.metadatumGridfs = mongoClient.getMetadatumGridFS();
	}

	public MongoCollection<Collection> getCollections() {
		return collections;
	}

	public MongoCollection<DataElement> getDataElements() {
		return dataElements;
	}

	public void close() {
		mongoClient.close();
	}

	public MetadatumGridFS getMetadatumGridFS() {
		return metadatumGridfs;
	}

	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String elementId) throws DatastoreException {
		for (Metadatum metadatum : metadata) {
			try {
				metadatumGridfs.upload(metadatum, elementId.toString());
			} catch (MongoGridFSException e) {
				logger.error(e.getMessage(), e);
				throw new DatastoreException("Inserting element metadata failed.");
			}
		}
		return metadata;
	}

	@Override
	public <T extends Element> T insert(T element) throws DatastoreException {
		element.setId(new ObjectId().toString());
		try {
			insertMetadata(element.getMetadata(), element.getId());
		} catch (MongoGridFSException e) {
			throw new DatastoreException("Bulk insertion failed.", e);
		}

		try {
			if (element instanceof Collection) {
				Collection collection = (Collection) element;

				for (DataElement dataElement : collection.getDataElements()) {
					dataElement.addCollection(collection);
					if (dataElement.getId() == null) {
						dataElement.setId(new ObjectId().toString());
					}
				}

				try {
					collections.insertOne(collection);
				} catch (MongoException e) {
					throw new DatastoreException("Collection " + collection.getName() + " insertion failed.", e);
				}

				for (DataElement dataElement : collection.getDataElements()) {
					try {
						insertMetadata(dataElement.getMetadata(), dataElement.getId());
					} catch (MongoGridFSException e) {
						logger.error(e.getMessage(), e);
					}
				}
				if (collection.getDataElements().size() > 0) {
					dataElements.insertMany(collection.getDataElements());

				}
			} else if (element instanceof DataElement) {
				DataElement dataElement = (DataElement) element;
				dataElements.insertOne(dataElement);

				if (dataElement.getCollections().size() > 0) {
					collections.insertMany(dataElement.getCollections());
				}
			}
		} catch (MongoException e) {
			metadatumGridfs.delete(element.getId());
			throw new DatastoreException("Inserting collection failed.", e);
		}
		return element;
	}

	public <T extends Element> List<T> insert(List<T> elements) throws DatastoreException {
		for (Element element : elements) {
			if (element.getId() != null) {
				element.setId(new ObjectId(element.getId()).toString());
			} else {
				element.setId(new ObjectId().toString());
			}
			try {
				insertMetadata(element.getMetadata(), element.getId());
			} catch (MongoGridFSException e) {
				throw new DatastoreException("Bulk insertion failed.", e);
			}
		}

		try {
			if (elements.get(0) instanceof Collection) {
				List<Collection> collectionList = (List<Collection>) elements;

				try {
					collections.insertMany(collectionList);
				} catch (MongoException e) {
					throw new DatastoreException("Collection bulk insertion failed.", e);
				}

				for (Collection collection : collectionList) {
					for (DataElement dataElement : collection.getDataElements()) {
						dataElement.addCollection(collection);
						dataElement.setId(new ObjectId().toString());
						try {
							insertMetadata(dataElement.getMetadata(), dataElement.getId());
						} catch (MongoGridFSException e) {
							logger.error(e.getMessage(), e);
						}
					}
					if (collection.getDataElements() != null && collection.getDataElements().size() > 0) {
						dataElements.insertMany(collection.getDataElements());
					}
				}
			} else if (elements.get(0) instanceof DataElement) {
				List<DataElement> dataElementList = (List<DataElement>) elements;
				dataElements.insertMany(dataElementList);

				/*
				 * if (dataElement.getCollections() != null &&
				 * dataElement.getCollections().size() > 0) {
				 * collections.insertMany(dataElement.getCollections()); }
				 */
			}
		} catch (MongoException e) {
			for (Element element : elements) {
				metadatumGridfs.delete(element.getId());
			}
			throw new DatastoreException("Bulk insertion failed.", e);
		}

		return elements;
	}

	@Override
	public Collection addToCollection(DataElement dataElement, ICriteria criteria) throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + new Document(criteria.getCriteria()).toJson().toString());

		if (dataElement.getId() == null) {
			dataElement.setId(new ObjectId().toString());
		}

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		updatedCollection = collections
				.findOneAndUpdate(
						BsonDocument
								.parse(new Document(
										criteria.getCriteria())
												.toJson()),
						BsonDocument.parse(new Document().append("$addToSet",
								new Document().append("dataElements", Documentizer.toOnlyIdDocument(dataElement)))
								.toJson()),
						options);

		if (updatedCollection != null) {
			dataElement.addCollection(updatedCollection);
			insert(dataElement);
		} else {
			logger.info("No collection updated");
		}
		return updatedCollection;
	}

	@Override
	public Collection addToCollection(List<DataElement> dataElementsList, ICriteria criteria)
			throws DatastoreException {
		Collection updatedCollection = null;
		
		logger.debug("addToCollection criteria query: " + new Document(criteria.getCriteria()).toJson().toString());

		List<Document> dataElementDocuments = dataElementsList.stream().map(new Function<DataElement, Document>() {
			@Override
			public Document apply(DataElement dataElement) {
				if (dataElement.getId() == null) {
					dataElement.setId(new ObjectId().toString());
				}
				return Documentizer.toOnlyIdDocument(dataElement);
			}
		}).collect(Collectors.toList());

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		updatedCollection = collections
				.findOneAndUpdate(
						BsonDocument
								.parse(new Document(
										criteria.getCriteria())
												.toJson()),
						BsonDocument
								.parse(new Document()
										.append("$addToSet",
												new Document().append("dataElements",
														new Document().append("$each", dataElementDocuments)))
										.toJson()),
						options);

		if (updatedCollection != null) {
			for (DataElement dataElement : dataElementsList) {
				dataElement.addCollection(updatedCollection);
			}
			insert(dataElementsList);
		} else {
			logger.info("No Collection updated");
		}
		return updatedCollection;
	}

	@Override
	public Element update(Element element) throws DatastoreException {
		return null;
	}

	@Override
	public <T extends Element> void delete(Criteria criteria, Class<T> elementSubtype)
			throws DatastoreException, IllegalElementSubtype {
		int deletedCount = 0;
		String subtype = elementSubtype.getSimpleName();

		logger.debug("Delete criteria query: " + new Document(criteria.getCriteria()).toJson().toString());
		
		List<T> toBeDeleted = find(new Query(criteria), elementSubtype).list();
		
		for (T element: toBeDeleted) {
			if (element instanceof DataElement) {
				try {
					logger.debug("Delete DataElement " + element.getId());
					dataElements.deleteOne(Filters.eq(ElementFields.id(), new ObjectId(element.getId())));
					logger.debug("Delete completed");
					deletedCount++;
				} catch(MongoException e) {
					throw new DatastoreException("Couldn't delete DataElement " + element.getId(), e);
				}
				// TODO: remove DataElement id from Collection
				metadatumGridfs.delete(element.getId());
			} else if (element instanceof Collection) {
				try {
					logger.debug("Delete Collection " + element.getId());
					collections.deleteOne(Filters.eq(ElementFields.id(), element.getId()));
					logger.debug("Delete completed");
					deletedCount++;
				} catch(MongoException e) {
					throw new DatastoreException("Couldn't delete Collection " + element.getId(), e);
				}
				// TODO: remove Collection id from DataElements
				metadatumGridfs.delete(element.getId());
			}
		}
		/*try {
			if (subtype.equals("DataElement")) {
				result = dataElements.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else if (subtype.equals("Collection")) {
				result = collections.deleteMany(BsonDocument.parse(new Document(criteria.getCriteria()).toJson()));
			} else {
				throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype");
			}
		} catch(MongoException e) {
			throw new DatastoreException("Couldn't delete element(s)", e);
		}*/

		if (deletedCount == toBeDeleted.size()) {
			logger.info("All " + deletedCount + " elements of type " + subtype + " were successfully deleted");
		} else if (deletedCount < toBeDeleted.size()) {
			logger.info(toBeDeleted.size() + " of " + deletedCount + " elements of type " + subtype + " were successfully deleted");			
		}
		
	}

	public void exists(Metadatum metadatum) {
		metadatumGridfs.exists(metadatum);
	}

	@Override
	public <T extends Element> Element find(String id, Class<T> elementSubtype) throws IllegalElementSubtype {
		String subtype = elementSubtype.getSimpleName();
		if (subtype.equals("DataElement")) {
			return dataElements.find(new Document().append(ElementFields.id(), id)).limit(1).first();
		} else if (subtype.equals("Collection")) {
			return collections.find(new Document().append(ElementFields.id(), id)).limit(1).first();
		} else {
			throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype.");
		}

	}

	@Override
	public <T extends Element> IQueryOptions<T> find(Query query, Class<T> elementSubtype)
			throws IllegalElementSubtype {
		return new QueryOptions<T>(query, this, elementSubtype);
		
		/*String subtype = elementSubtype.getSimpleName();
		if (subtype.equals("DataElement")) {
			return (QueryOptions<T>) new QueryOptions<DataElement>(query, this);
		} else if (subtype.equals("Collection")) {
			return (QueryOptions<T>) new QueryOptions<Collection>(query, this);
		} else {
			throw new IllegalElementSubtype(subtype + ".class is not a valid element subtype.");
		}*/
	}

	/*
	 * public List<Element> find(Element element) throws DatastoreException {
	 * List<Element> elements = new ArrayList<>(); if (element.getMetadata() !=
	 * null && (element.getMetadata().size() == 0 ||
	 * element.getMetadata().stream().allMatch(metadatum -> metadatum.getValue()
	 * == null))) { MongoCursor<Element> cursor =
	 * elementCollection.find(buildElementBson(element)).iterator(); try { while
	 * (cursor.hasNext()) { Element elementFromDB = cursor.next(); for
	 * (Metadatum metadatum : elementFromDB.getMetadata()) { Metadatum
	 * gridFSDownload = metadatumGridfs.download(metadatum.getId());
	 * metadatum.setValue(gridFSDownload.getValue()); }
	 * elements.add(elementFromDB); } } finally { cursor.close(); } } else {
	 * metadatumGridfs.find(element.getMetadata());
	 * 
	 * MongoCursor<Element> cursor =
	 * elementCollection.find(buildElementBson(element)).iterator(); try { while
	 * (cursor.hasNext()) { Element elementFromDB = cursor.next(); for
	 * (Metadatum metadatum : elementFromDB.getMetadata()) { Metadatum
	 * gridFSDownload = metadatumGridfs.download(metadatum.getId());
	 * metadatum.setValue(gridFSDownload.getValue()); }
	 * elements.add(elementFromDB); } } finally { cursor.close(); } }
	 * 
	 * return elements; }
	 */

	@Override
	public void remove(Element dataElement, Collection collection) throws DatastoreException {

	}


	/*
	 * private ElementBson buildElementBson(Element element) { ElementBson
	 * elementBson = null; if (element instanceof DataElement) { elementBson =
	 * new
	 * DataElementBsonBuilder().id(element.getId()).endpoint(element.getEndpoint
	 * ()) .name(element.getName()).metadata(element.getMetadata())
	 * .systemicMetadata(element.getSystemicMetadata())
	 * .dataElement(((DataElement) element).getDataElement()).build(); } else if
	 * (element instanceof Collection) { elementBson = new
	 * CollectionBsonBuilder().id(element.getId()).endpoint(element.getEndpoint(
	 * )) .name(element.getName()).metadata(element.getMetadata())
	 * .systemicMetadata(element.getSystemicMetadata())
	 * .dataElements(((Collection) element).getDataElements()).build(); } return
	 * elementBson; }
	 */
}
