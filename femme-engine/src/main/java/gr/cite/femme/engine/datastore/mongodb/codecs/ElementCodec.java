package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.SystemicMetadata;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Element;

public abstract class ElementCodec<T extends Element> implements CollectibleCodec<T> {
	private CodecRegistry codecRegistry;
	
	public ElementCodec(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	CodecRegistry getCodecRegistry() {
		return this.codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		
		/*if (encoderContext.isEncodingCollectibleDocument()) {*/
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}			
		/*}*/
		if (value.getId() != null) {
			writer.writeObjectId(FieldNames.ID, new ObjectId(value.getId()));
		}
		if (value.getName() != null) {
			writer.writeString(FieldNames.NAME, value.getName());
		}
		if (value.getEndpoint() != null) {
			writer.writeString(FieldNames.ENDPOINT, value.getEndpoint());
		}
		if (value instanceof DataElement) {
			DataElement dataElement = (DataElement) value;
			//writeEmdeddedDataElements(writer, encoderContext, dataElement.getDataElements());

			if (dataElement.getDataElements() != null && dataElement.getDataElements().size() > 0) {
				writer.writeStartArray(FieldNames.DATA_ELEMENTS);
				for (DataElement subDataElement: dataElement.getDataElements()) {
					encoderContext.encodeWithChildContext(codecRegistry.get(DataElement.class), writer, subDataElement);
				}
				writer.writeEndArray();
			}
				
			if (dataElement.getCollections() != null && dataElement.getCollections().size() > 0) {
				writer.writeName(FieldNames.COLLECTIONS);
				writer.writeStartArray();
				for (Collection collection: dataElement.getCollections()) {
					//generateIdIfAbsentFromDocument((T) collection);

					writer.writeStartDocument();
					if (collection.getId() != null) {
						writer.writeObjectId(FieldNames.ID, new ObjectId(collection.getId()));
					}
					if (collection.getName() != null) {
						writer.writeString(FieldNames.NAME, collection.getName());
					}
					if (collection.getEndpoint() != null) {
						writer.writeString(FieldNames.ENDPOINT, collection.getEndpoint());
					}
					writer.writeEndDocument();
				}
				writer.writeEndArray();
			}
		}
		/*if (value.getMetadata() != null && value.getMetadata().size() > 0) {
			writer.writeStartArray(FieldNames.METADATA);
			for (Metadatum metadatum : value.getMetadata()) {
				if (value.getId() != null) {
					metadatum.setElementId(value.getId());
				}
				encoderContext.encodeWithChildContext(codecRegistry.get(Metadatum.class), writer, metadatum);
			}
			writer.writeEndArray();
		}*/
		if (value.getSystemicMetadata() != null) {
			writer.writeName(FieldNames.SYSTEMIC_METADATA);
			encoderContext.encodeWithChildContext(codecRegistry.get(SystemicMetadata.class), writer, value.getSystemicMetadata());
		}
		writer.writeEndDocument();
	}
	
	/*private void writeMetadata(BsonWriter writer, Metadatum metadatum, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (metadatum.getId() != null) {
			writer.writeObjectId(FieldNames.METADATA_FILE_ID, new ObjectId(metadatum.getId()));			
		} else {
			writer.writeObjectId(FieldNames.ID, new ObjectId());
		}
		
		
		*//* writer.writeString(METADATUM_FILENAME_KEY, file.getSecond()); *//*
		if (metadatum.getName() != null) {
			writer.writeString(FieldNames.NAME, metadatum.getName());
		}
		if (metadatum.getContentType() != null) {
			writer.writeString(FieldNames.CONTENT_TYPE, metadatum.getContentType());
		}

		writer.writeEndDocument();
	}*/
	
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
	
	/*private void writeEmdeddedDataElements(BsonWriter writer, EncoderContext encoderContext, List<DataElement> dataElements) {
		if (dataElements != null && dataElements.size() > 0) {
			writer.writeStartArray(FieldNames.DATA_ELEMENTS);
			for (DataElement dataElement: dataElements) {
				encoderContext.encodeWithChildContext(codecRegistry.get(Element.class), writer, dataElement);
			}
			writer.writeEndArray();
		}
	}*/

	@Override
	public abstract T decode(BsonReader reader, DecoderContext decoderContext); /*{
		String id = null, name = null, endpoint = null;
		List<Metadatum> metadata = null;
		SystemicMetadata systemicMetadata =  null;
		List<DataElement> embeddedDataElements = null;
		List<Collection> dataElementCollections = null;
		boolean isDataElement = false;
		
		reader.readStartDocument();
		
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(FieldNames.ID)) {
            	id = reader.readObjectId().toString();
            } else if (fieldName.equals(FieldNames.NAME)) {
            	name = reader.readString();
            } else if (fieldName.equals(FieldNames.ENDPOINT)) {
            	endpoint = reader.readString();
            } else if (fieldName.equals(FieldNames.METADATA)) {
            	metadata = new ArrayList<>();
        		reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			Metadatum metadatum = codecRegistry.get(Metadatum.class).decode(reader, decoderContext);
        			metadatum.setElementId(id);
        			metadata.add(metadatum);
        		}
        		reader.readEndArray();
            } else if (fieldName.equals(FieldNames.SYSTEMIC_METADATA)) {
            	if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            		systemicMetadata = codecRegistry.get(SystemicMetadata.class).decode(reader, decoderContext);            		
            	}
            } else if (fieldName.equals(FieldNames.DATA_ELEMENTS)) {
            	embeddedDataElements = new ArrayList<>();
            	reader.readStartArray();
        		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        			embeddedDataElements.add((DataElement) codecRegistry.get(Element.class).decode(reader, decoderContext));
        		}
        		reader.readEndArray();
            } else if (fieldName.equals(FieldNames.COLLECTIONS)) {
            	isDataElement = true;
            	dataElementCollections = new ArrayList<>();

            	reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                	reader.readStartDocument();
                	Collection collection = new Collection();
                	while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
	                    String collectionFieldName = reader.readName();
	                    
	                	if (collectionFieldName.equals(FieldNames.ID)) {
	        				collection.setId(reader.readObjectId().toString());
	        			} else if (collectionFieldName.equals(FieldNames.NAME)) {
	        				collection.setName(reader.readString());
	        			} else if (collectionFieldName.equals(FieldNames.ENDPOINT)) {
	        				collection.setEndpoint(reader.readString());
	        			}
                	}
                	reader.readEndDocument();
                	dataElementCollections.add(collection);
                }
        		reader.readEndArray();
        		
        		
            }
        }

        reader.readEndDocument();
        
        if (isDataElement) {
        	return DataElement.builder()
        			.id(id).name(name).endpoint(endpoint)
        			.metadata(metadata).systemicMetadata(systemicMetadata)
        			.dataElements(embeddedDataElements).collections(dataElementCollections).execute();
        	*//*return new DataElement(id, name, endpoint, metadata, systemicMetadata, embeddedDataElements, dataElementCollections);*//*
        } else {
        	return Collection.builder().id(id).name(name).endpoint(endpoint).metadata(metadata).systemicMetadata(systemicMetadata).execute();
        	*//*return new Collection(id, name, endpoint, metadata, systemicMetadata, collectionDataElements);*//*
        }
	}*/

	@Override
	public T generateIdIfAbsentFromDocument(T element) {
		if (!documentHasId(element)) {
			element.setId(new ObjectId().toString());
		}
		return element;
	}

	@Override
	public boolean documentHasId(T element) {
		return element.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(T element) {
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
