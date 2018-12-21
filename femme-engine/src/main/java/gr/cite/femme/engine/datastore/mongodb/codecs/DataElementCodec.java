package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.core.model.FieldNames;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.model.Collection;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;

public class DataElementCodec extends ElementCodec<DataElement> {
	public DataElementCodec(CodecRegistry codecRegistry) {
		super(codecRegistry);
	}

	@Override
	public DataElement decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, name = null, endpoint = null;
		List<Metadatum> metadata = null;
		SystemicMetadata systemicMetadata =  null;
		List<DataElement> embeddedDataElements = null;
		List<Collection> dataElementCollections = null;

		reader.readStartDocument();

		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();

			if (fieldName.equals(FieldNames.ID)) {
				id = reader.readObjectId().toString();
			} else if (fieldName.equals(FieldNames.NAME)) {
				name = reader.readString();
			} else if (fieldName.equals(FieldNames.ENDPOINT)) {
				endpoint = reader.readString();
			}/* else if (fieldName.equals(FieldNames.METADATA)) {
				metadata = new ArrayList<>();
				reader.readStartArray();
				while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
					Metadatum metadatum = this.getCodecRegistry().get(Metadatum.class).decode(reader, decoderContext);
					metadatum.setElementId(id);
					metadata.add(metadatum);
				}
				reader.readEndArray();
			}*/ else if (fieldName.equals(FieldNames.SYSTEMIC_METADATA)) {
				if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
					systemicMetadata = this.getCodecRegistry().get(SystemicMetadata.class).decode(reader, decoderContext);
				}
			} else if (fieldName.equals(FieldNames.DATA_ELEMENTS)) {
				embeddedDataElements = new ArrayList<>();
				reader.readStartArray();
				while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
					embeddedDataElements.add(this.getCodecRegistry().get(DataElement.class).decode(reader, decoderContext));
				}
				reader.readEndArray();
			} else if (fieldName.equals(FieldNames.COLLECTIONS)) {
				dataElementCollections = new ArrayList<>();

				reader.readStartArray();
				while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
					//reader.readStartDocument();
					Collection collection = new Collection();
					collection.setId(reader.readString());
					
					/*while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
						String collectionFieldName = reader.readName();

						if (collectionFieldName.equals(FieldNames.ID)) {
							collection.setId(reader.readObjectId().toString());
						} else if (collectionFieldName.equals(FieldNames.NAME)) {
							collection.setName(reader.readString());
						} else if (collectionFieldName.equals(FieldNames.ENDPOINT)) {
							collection.setEndpoint(reader.readString());
						}
					}*/
					//reader.readEndDocument();
					dataElementCollections.add(collection);
				}
				reader.readEndArray();


			}
		}

		reader.readEndDocument();

		return DataElement.builder()
				.id(id).name(name).endpoint(endpoint)
				.metadata(metadata).systemicMetadata(systemicMetadata)
				.dataElements(embeddedDataElements).collections(dataElementCollections).build();

	}

	@Override
	public Class<DataElement> getEncoderClass() {
		return DataElement.class;
	}
}
