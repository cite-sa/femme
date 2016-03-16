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
	
	private static final String DATA_ELEMENT_DATA_ELEMENT_KEY = "dataElement";
	private static final String DATA_ELEMENT_COLLECTIONS_KEY = "collections";
	
	private static final String COLLECTION_DATA_ELEMENTS_KEY = "dataElements";
	/*private static final String COLLECTION_IS_COLLECTION_KEY = "isCollection";*/
	
	private CodecRegistry codecRegistry;
	
	public ElementCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Element value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		
		writer.writeObjectId(ELEMENT_ID_KEY, new ObjectId(value.getId()));
		if (value.getName() != null) {
			writer.writeString(ELEMENT_NAME_KEY, value.getName());
		}
		if (value.getEndpoint() != null) {
			writer.writeString(ELEMENT_ENDPOINT_KEY, value.getEndpoint());
		}
		
		if (value instanceof DataElement) {
			if (((DataElement) value).getDataElement() != null) {
				writeEmdeddedDataElement(writer, encoderContext, (DataElement) value);
			}
			if (((DataElement) value).getCollections() != null) {
				writeEmbeddedCollections(writer, encoderContext, (DataElement) value);
			}
		} else if (value instanceof Collection) {
			writeEmdeddedDataElements(writer, encoderContext, (Collection) value);
		}
		
		if (value.getMetadata() != null && value.getMetadata().size() > 0) {
			writer.writeStartArray(ELEMENT_METADATA_KEY);
			for (Metadatum metadatum : value.getMetadata()) {
				metadatum.setElementId(value.getId());
				encoderContext.encodeWithChildContext(codecRegistry.get(Metadatum.class), writer, metadatum);
			}
			writer.writeEndArray();
		}
		
		// TODO : Systemic metadata
		if (value.getSystemicMetadata() != null) {
			writer.writeName(ELEMENT_SYSTEMIC_METADATA_KEY);
			encoderContext.encodeWithChildContext(codecRegistry.get(SystemicMetadata.class), writer, value.getSystemicMetadata());
		}
		writer.writeEndDocument();
	}
	
	private void writeEmdeddedDataElement(BsonWriter writer, EncoderContext encoderContext, DataElement dataElement) {
		if (dataElement.getDataElement() != null) {
			writer.writeName(DATA_ELEMENT_DATA_ELEMENT_KEY);
			DataElement newDataElement = dataElement.getDataElement(); 
			encoderContext.encodeWithChildContext(this.codecRegistry.get(Element.class), writer, newDataElement);
		}
	}
	
	private void writeEmbeddedCollections(BsonWriter writer, EncoderContext encoderContext, DataElement dataElement) {
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
		DataElement embeddedDataElement = null;
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
        			metadata.add(codecRegistry.get(Metadatum.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            } else if (fieldName.equals("systemicMetadata")) {
            	if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		systemicMetadata = codecRegistry.get(SystemicMetadata.class).decode(reader, decoderContext);            		
            	}
            } else if (fieldName.equals(DATA_ELEMENT_DATA_ELEMENT_KEY)) {
            	if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		embeddedDataElement = (DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext);            		
            	}
            } else if (fieldName.equals(DATA_ELEMENT_COLLECTIONS_KEY)) {
            	dataElementCollections = new ArrayList<>();
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			dataElementCollections.add((Collection) codecRegistry.get(Element.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            } else if (fieldName.equals(COLLECTION_DATA_ELEMENTS_KEY)) {
            	isCollection = true;
            	collectionDataElements = new ArrayList<>();
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			collectionDataElements.add((DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            }
        }

        reader.readEndDocument();
        
        // Element is a collection
        if (isCollection) {
        	return new Collection(id, name, endpoint, metadata, systemicMetadata, collectionDataElements);
        } else { // Element is a data element
        	return new DataElement(id, name, endpoint, metadata, systemicMetadata, embeddedDataElement, dataElementCollections);
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
		if (!documentHasId(element))
	    {
			if (element instanceof DataElement) {
				throw new IllegalStateException("The data element does not contain an _id");				
			} else if (element instanceof Collection) {
				throw new IllegalStateException("The collection does not contain an _id");
			}
	        
	    }
	    return new BsonString(element.getId());
	}
}
