package gr.cite.exmms.datastore.mongo.codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.DataElementBuilder;
import gr.cite.exmms.core.DataElementMetadatum;
import gr.cite.exmms.core.Element;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.core.SystemicMetadatum;

public class MongoSerializer {
	private static final String DATA_ELEMENT_ID_KEY = "_id";
	private static final String DATA_ELEMENT_NAME_KEY = "name";
	private static final String DATA_ELEMENT_ENDPOINT_KEY = "endpoint";
	private static final String DATA_ELEMENT_METADATA_KEY = "metadata";
	private static final String DATA_ELEMENT_SYSTEMIC_METADATA_KEY = "metadata";
	private static final String DATA_ELEMENT_DATA_ELEMENT_KEY = "dataElement";

	private static final String COLLECTION_ID_KEY = "_id";
	private static final String COLLECTION_NAME_KEY = "name";
	private static final String COLLECTION_ENDPOINT_KEY = "endpoint";
	private static final String COLLECTION_METADATA_KEY = "metadata";
	private static final String COLLECTION_SYSTEMIC_METADATA_KEY = "metadata";
	private static final String COLLECTION_DATA_ELEMENTS_KEY = "dataElements";
	private static final String COLLECTION_IS_COLLECTION_KEY = "isCollection";

	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_VALUE_KEY = "value";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";

	
	public static <T extends Element> Document createDocument(T element) {
		Document document = null;
		if (element instanceof DataElement) {
			document = createDocumentFromDataElement((DataElement) element);
		} else if (element instanceof Collection) {
			document = createDocumentFromCollection((Collection) element);
		}
		return document;
	}
	
	public static Document createDocument(Metadatum metadatum) {
		Document metadatumDocument = new Document();
		if (metadatum.getName() != null) {
			metadatumDocument.append(METADATUM_NAME_KEY, metadatum.getName());
		}
		if (metadatum.getName() != null) {
			metadatumDocument.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getName());
		}
		return metadatumDocument;
	}

	public static Element createElement(Document document) {
		Element element = null;
		if (!document.containsKey(COLLECTION_IS_COLLECTION_KEY)) {
			element = createDataElement(document);
		} else if (document.containsKey(COLLECTION_IS_COLLECTION_KEY)) {
			element = createCollection(document);
		}
		return element;
	}

	private static Document createDocumentFromDataElement(DataElement dataElement) {
		Document newDataElementDocument = new Document();
		if (dataElement.getName() != null) {
			newDataElementDocument.append(DATA_ELEMENT_NAME_KEY, dataElement.getName());
		}
		if (dataElement.getEndpoint() != null) {
			newDataElementDocument.append(DATA_ELEMENT_ENDPOINT_KEY, dataElement.getEndpoint());
		}
		
		if (dataElement.getMetadata().size() > 0) {
			newDataElementDocument.append(DATA_ELEMENT_METADATA_KEY,
					dataElement.getMetadata().stream().map(new Function<Metadatum, Document>() {
						@Override
						public Document apply(Metadatum metadatum) {
							return new Document()
									.append(METADATUM_ID_KEY, metadatum.getId())
									.append(METADATUM_NAME_KEY, metadatum.getName())
									/*.append(METADATUM_VALUE_KEY, metadatum.getValue())*/
									.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
						}
					}).collect(Collectors.toList()));
		}
		if (dataElement.getSystemicMetadata().size() > 0) {
			newDataElementDocument.append(DATA_ELEMENT_SYSTEMIC_METADATA_KEY,
					dataElement.getSystemicMetadata().stream().map(new Function<SystemicMetadatum, Document>() {
						@Override
						public Document apply(SystemicMetadatum metadatum) {
							return new Document().append(METADATUM_NAME_KEY, metadatum.getName())
									.append(METADATUM_VALUE_KEY, metadatum.getValue())
									.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
						}
					}).collect(Collectors.toList()));
		}
		if (dataElement.getDataElement() != null) {
			newDataElementDocument.append(DATA_ELEMENT_DATA_ELEMENT_KEY,
					createDocumentFromDataElement(dataElement.getDataElement()));
		}

		return newDataElementDocument;
	}

	private static Document createDocumentFromCollection(Collection collection) {
		List<Document> dataElements = collection.getDataElements().stream().map(new Function<DataElement, Document>() {
			@Override
			public Document apply(DataElement dataElement) {
				return createDocument(dataElement);
			}
		}).collect(Collectors.toList());

		Document newCollectionDocument = new Document();

		if (collection.getName() != null) {
			newCollectionDocument.append(COLLECTION_NAME_KEY, collection.getName());
		}
		if (collection.getEndpoint() != null) {
			newCollectionDocument.append(COLLECTION_ENDPOINT_KEY, collection.getEndpoint());
		}
		
		
		if (collection.getMetadata().size() > 0) {
			newCollectionDocument.append(COLLECTION_METADATA_KEY,
					collection.getMetadata().stream().map(new Function<Metadatum, Document>() {
						@Override
						public Document apply(Metadatum metadatum) {
							return new Document().append(METADATUM_NAME_KEY, metadatum.getName())
									.append(METADATUM_VALUE_KEY, metadatum.getValue())
									.append(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
						}
					}).collect(Collectors.toList()));
		}
		if (collection.getSystemicMetadata().size() > 0) {
			newCollectionDocument.append(COLLECTION_SYSTEMIC_METADATA_KEY,
					collection.getSystemicMetadata().stream().map(new Function<SystemicMetadatum, Document>() {
						@Override
						public Document apply(SystemicMetadatum metadatum) {
							return new Document().append(METADATUM_NAME_KEY, metadatum.getName())
									.append(METADATUM_NAME_KEY, metadatum.getValue())
									.append(METADATUM_NAME_KEY, metadatum.getContentType());
						}
					}).collect(Collectors.toList()));
		}
		
		if (collection.getDataElements().size() > 0) {
			newCollectionDocument.append(COLLECTION_DATA_ELEMENTS_KEY, dataElements);
		}
		newCollectionDocument.append(COLLECTION_IS_COLLECTION_KEY, 1);

		return newCollectionDocument;
	}

	private static DataElement createDataElement(Document document) {
		Document dataElementDocument = document.get(DATA_ELEMENT_DATA_ELEMENT_KEY, Document.class);
		DataElement dataElement = null;
		if (dataElementDocument != null) {
			dataElement = createDataElement(dataElementDocument);
		}
		
		List<Metadatum> metadata = null;
		List<Document> metadataDocument = document.get(DATA_ELEMENT_METADATA_KEY, List.class);
		if (metadataDocument != null) {
			metadata = new ArrayList<>();
			metadata = metadataDocument.stream().map(new Function<Document, Metadatum>() {
				@Override
				public Metadatum apply(Document document) {
					return MongoSerializer.createMetadatum(document);
				}
			}).collect(Collectors.toList());
		}
		
		return new DataElementBuilder()
			.id(document.get(DATA_ELEMENT_ID_KEY, ObjectId.class).toString())
			.endpoint(document.getString(DATA_ELEMENT_ENDPOINT_KEY))
			.name(document.getString(DATA_ELEMENT_NAME_KEY))
			.metadata(metadata)
			.dataElement(dataElement)
			.build();
	}

	private static Collection createCollection(Document document) {
		return null;
	}
	
	public static Metadatum createMetadatum(Document document) {
		return new Metadatum(document.getString(METADATUM_NAME_KEY), document.getString(METADATUM_VALUE_KEY),
				document.getString(METADATUM_CONTENT_TYPE_KEY));
	}
}
