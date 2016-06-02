package gr.cite.femme.datastore.mongodb.codecs;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import gr.cite.femme.core.Collection;
import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.core.SystemicMetadata;

public class ElementCodec implements CollectibleCodec<Element> {
	private static final String ELEMENT_ID_KEY = "_id";
	private static final String ELEMENT_NAME_KEY = "name";
	private static final String ELEMENT_ENDPOINT_KEY = "endpoint";
	private static final String ELEMENT_METADATA_KEY = "metadata";
	private static final String ELEMENT_SYSTEMIC_METADATA_KEY = "systemicMetadata";
	
	private static final String DATA_ELEMENT_DATA_ELEMENTS_KEY = "subDataElements";
	private static final String DATA_ELEMENT_COLLECTIONS_KEY = "collections";
	
	private static final String COLLECTION_DATA_ELEMENTS_KEY = "dataElements";
	/*private static final String COLLECTION_IS_COLLECTION_KEY = "isCollection";*/
	
	private static final String METADATUM_ID_KEY = "_id";
	private static final String METADATUM_FILENAME_KEY = "fileName";
	private static final String METADATUM_FILE_ID_KEY = "fileId";
	private static final String METADATUM_ELEMENT_ID_KEY = "elementId";
	private static final String METADATUM_NAME_KEY = "name";
	private static final String METADATUM_CONTENT_TYPE_KEY = "contentType";
	
	private CodecRegistry codecRegistry;
	
	public ElementCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Element value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		/*if (encoderContext.isEncodingCollectibleDocument()) {*/
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}			
		/*}*/
		
		if (value.getId() != null) {			
			writer.writeObjectId(ELEMENT_ID_KEY, new ObjectId(value.getId()));
		}
		if (value.getName() != null) {
			writer.writeString(ELEMENT_NAME_KEY, value.getName());
		}
		if (value.getEndpoint() != null) {
			writer.writeString(ELEMENT_ENDPOINT_KEY, value.getEndpoint());
		}
		
		if (value instanceof DataElement) {
			DataElement dataElement = (DataElement) value;
			writeEmdeddedDataElements(writer, encoderContext, dataElement.getDataElements());
				
			if (dataElement.getCollections() != null) {
				/*writeEmbeddedCollections(writer, encoderContext, (DataElement) value);*/
				
				writer.writeName(DATA_ELEMENT_COLLECTIONS_KEY);
				writer.writeStartArray();
				for (Collection collection: dataElement.getCollections()) {
					writer.writeStartDocument();
					if (!documentHasId(collection)) {
						generateIdIfAbsentFromDocument(collection);
					}
					writer.writeObjectId(ELEMENT_ID_KEY, new ObjectId(collection.getId()));
					/*writer.writeString(ELEMENT_NAME_KEY, collection.getName());*/
					writer.writeEndDocument();
				}
				writer.writeEndArray();
			}
		} else if (value instanceof Collection) {
			/*writeEmdeddedDataElements(writer, encoderContext, (Collection) value);*/
			
			Collection collection = (Collection) value;
			if (collection.getDataElements() != null) {
				writer.writeName(COLLECTION_DATA_ELEMENTS_KEY);
				writer.writeStartArray();
				for (DataElement dataElement: collection.getDataElements()) {
					writer.writeStartDocument();
					if (!documentHasId(dataElement)) {
						generateIdIfAbsentFromDocument(dataElement);
					}
					writer.writeObjectId(ELEMENT_ID_KEY, new ObjectId(dataElement.getId()));
					/*writer.writeString(ELEMENT_NAME_KEY, dataElement.getName());*/
					writer.writeEndDocument();
				}
				writer.writeEndArray();
			}
		}
		
		/*if (value.getMetadata() != null && value.getMetadata().size() > 0) {
			if (!encoderContext.isEncodingCollectibleDocument()) {
				writer.writeName(ELEMENT_METADATA_KEY);
				writer.writeStartDocument();
				writer.writeName("$elemMatch");
			}*/
		if (value.getMetadata() != null) {
			writer.writeStartArray(ELEMENT_METADATA_KEY);
		
			for (Metadatum metadatum : value.getMetadata()) {
				if (value.getId() != null) {
					metadatum.setElementId(value.getId());
				}
			
				encoderContext.encodeWithChildContext(codecRegistry.get(Metadatum.class), writer, metadatum);
			}
			/*writeMetadata(writer, metadatum, encoderContext);*/
			
			/*if (!encoderContext.isEncodingCollectibleDocument()) {
				writer.writeEndDocument();
			} else {*/
				writer.writeEndArray();
			/*}*/
		}
		
		// TODO : Systemic metadata
		if (value.getSystemicMetadata() != null) {
			writer.writeName(ELEMENT_SYSTEMIC_METADATA_KEY);
			encoderContext.encodeWithChildContext(codecRegistry.get(SystemicMetadata.class), writer, value.getSystemicMetadata());
		}
		writer.writeEndDocument();
	}
	
	private void writeMetadata(BsonWriter writer, Metadatum metadatum, EncoderContext encoderContext) {
		writer.writeStartDocument();

		/*if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}*/
		/*if (encoderContext.isEncodingCollectibleDocument()) {*/
			
		/*}*/
		if (metadatum.getId() != null) {
			writer.writeObjectId(METADATUM_FILE_ID_KEY, new ObjectId(metadatum.getId()));			
		} else {
			writer.writeObjectId(METADATUM_ID_KEY, new ObjectId());
		}
		
		
		/* writer.writeString(METADATUM_FILENAME_KEY, file.getSecond()); */
		if (metadatum.getName() != null) {
			writer.writeString(METADATUM_NAME_KEY, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			writer.writeString(METADATUM_CONTENT_TYPE_KEY, metadatum.getContentType());
		}

		writer.writeEndDocument();
	}
	
	/*private void writeEmdeddedDataElement(BsonWriter writer, EncoderContext encoderContext, DataElement dataElement) {
		if (dataElement.getDataElement() != null) {
			writer.writeName(DATA_ELEMENT_DATA_ELEMENT_KEY);
			DataElement newDataElement = dataElement.getDataElement(); 
			encoderContext.encodeWithChildContext(this.codecRegistry.get(Element.class), writer, newDataElement);
		}
	}*/
	
	/*private void writeEmbeddedCollections(BsonWriter writer, EncoderContext encoderContext, DataElement dataElement) {
		if (dataElement.getCollections() != null) {
			writer.writeStartArray(DATA_ELEMENT_COLLECTIONS_KEY);
			for (Collection collection : dataElement.getCollections()) {
				encoderContext.encodeWithChildContext(codecRegistry.get(Element.class), writer, collection);
			}
			writer.writeEndArray();
		}
	}
	
	private void writeEmdeddedDataElements(BsonWriter writer, EncoderContext encoderContext, Collection collection) {
		if (collection.getDataElements() != null) {
			writer.writeStartArray(COLLECTION_DATA_ELEMENTS_KEY);
			for (DataElement dataElement: collection.getDataElements()) {
				encoderContext.encodeWithChildContext(codecRegistry.get(Element.class), writer, dataElement);
			}
			writer.writeEndArray();
		}
	}*/
	
	private void writeEmdeddedDataElements(BsonWriter writer, EncoderContext encoderContext, List<DataElement> dataElements) {
		if (dataElements != null) {
			writer.writeStartArray(DATA_ELEMENT_DATA_ELEMENTS_KEY);
			for (DataElement dataElement: dataElements) {
				encoderContext.encodeWithChildContext(codecRegistry.get(Element.class), writer, dataElement);
			}
			writer.writeEndArray();
		}
	}

	@Override
	public Class<Element> getEncoderClass() {
		return Element.class;
	};

	@Override
	public Element decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, name = null, endpoint = null;
		List<Metadatum> metadata = null;
		SystemicMetadata systemicMetadata =  null;
		List<DataElement> embeddedDataElements = null;
		List<Collection> dataElementCollections = null;
		List<DataElement> collectionDataElements = null;
		boolean isCollection = false;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(ELEMENT_ID_KEY)) {
            	id = reader.readObjectId().toString();
            } else if (fieldName.equals(ELEMENT_NAME_KEY)) {
            	name = reader.readString();
            } else if (fieldName.equals(ELEMENT_ENDPOINT_KEY)) {
            	endpoint = reader.readString();
            } else if (fieldName.equals(ELEMENT_METADATA_KEY)) {
            	metadata = new ArrayList<>();
        		reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			Metadatum metadatum = codecRegistry.get(Metadatum.class).decode(reader, decoderContext);
        			metadatum.setElementId(id);
        			metadata.add(metadatum);
        		}
        		reader.readEndArray();
            } else if (fieldName.equals("systemicMetadata")) {
            	if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		systemicMetadata = codecRegistry.get(SystemicMetadata.class).decode(reader, decoderContext);            		
            	}
            } else if (fieldName.equals(DATA_ELEMENT_DATA_ELEMENTS_KEY)) {
            	/*if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		embeddedDataElement = (DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext);            		
            	}*/
            	
            	embeddedDataElements = new ArrayList<>();
            	
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			embeddedDataElements.add((DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            } else if (fieldName.equals(DATA_ELEMENT_COLLECTIONS_KEY)) {
            	dataElementCollections = new ArrayList<>();
            	
            	/*reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			dataElementCollections.add((Collection) codecRegistry.get(Element.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();*/
            	
            	reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                	/*System.out.println(reader.readBsonType());*/
                	reader.readStartDocument();
                	/*System.out.println(reader.readObjectId().toString());*/
                    String collectionFieldName = reader.readName();
                    
                	if (collectionFieldName.equals(ELEMENT_ID_KEY)) {
        				Collection collection = new Collection();
        				collection.setId(reader.readObjectId().toString());
        				dataElementCollections.add(collection);
        			}
                	reader.readEndDocument();
                }
        		reader.readEndArray();
        		
        		
            } else if (fieldName.equals(COLLECTION_DATA_ELEMENTS_KEY)) {
            	isCollection = true;
            	collectionDataElements = new ArrayList<>();
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			collectionDataElements.add((DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext));
        			/*String embeddedDataElementFieldName = reader.readName();
        			if (embeddedDataElementFieldName.equals(ELEMENT_ID_KEY)) {
        				dataElement.setId(reader.readObjectId().toString()); 				
        			} else if (embeddedDataElementFieldName.equals(ELEMENT_NAME_KEY)) {
        				dataElement.setName(reader.readString());
        			}
        			collectionDataElements.add(dataElement);*/
        		}
        		reader.readEndArray();
            }
        }

        reader.readEndDocument();
        
        // Element is a Collection
        if (isCollection) {
        	return new Collection(id, name, endpoint, metadata, systemicMetadata, collectionDataElements);
        } else { // Element is a DataElement
        	return new DataElement(id, name, endpoint, metadata, systemicMetadata, embeddedDataElements, dataElementCollections);
        }
	}

	@Override
	public Element generateIdIfAbsentFromDocument(Element element) {
		if (!documentHasId(element)) {
			element.setId(new ObjectId().toString());
		}
		return element;
	}

	@Override
	public boolean documentHasId(Element element) {
		return element.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(Element element) {
		if (!documentHasId(element)) {
			if (element instanceof DataElement) {
				throw new IllegalStateException("The data element does not contain an _id");				
			} else if (element instanceof Collection) {
				throw new IllegalStateException("The collection does not contain an _id");
			}
	    }
	    return new BsonString(element.getId());
	}
}
