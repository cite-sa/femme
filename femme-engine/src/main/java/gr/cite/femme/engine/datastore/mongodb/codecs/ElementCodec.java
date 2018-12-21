package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.FieldNames;
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

import java.util.List;

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
		
		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
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
			writeSubDataElements(dataElement.getDataElements(), writer, encoderContext);
			writeCollections(dataElement.getCollections(), writer, encoderContext);
		}
		
		if (value.getSystemicMetadata() != null) {
			writer.writeName(FieldNames.SYSTEMIC_METADATA);
			encoderContext.encodeWithChildContext(codecRegistry.get(SystemicMetadata.class), writer, value.getSystemicMetadata());
		}
		writer.writeEndDocument();
	}

	private void writeSubDataElements(List<DataElement> subDataElements, BsonWriter writer, EncoderContext encoderContext) {
		if (subDataElements != null && subDataElements.size() > 0) {
			writer.writeStartArray(FieldNames.DATA_ELEMENTS);
			for (DataElement subDataElement: subDataElements) {
				encoderContext.encodeWithChildContext(codecRegistry.get(DataElement.class), writer, subDataElement);
			}
			writer.writeEndArray();
		}
	}
	
	private void writeCollections(List<Collection> collections, BsonWriter writer, EncoderContext encoderContext) {
		if (collections != null && collections.size() > 0) {
			writer.writeStartArray(FieldNames.COLLECTIONS);
			for (Collection collection: collections) {
				if (collection.getId() != null) {
					writer.writeString(collection.getId());
				}
			}
			writer.writeEndArray();
		}
	}
	
	@Override
	public abstract T decode(BsonReader reader, DecoderContext decoderContext);
	
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
				throw new IllegalStateException("The DataElement does not contain an _id");
			} else if (element instanceof Collection) {
				throw new IllegalStateException("The Collection does not contain an _id");
			}
	    }
	    return new BsonString(element.getId());
	}
}
