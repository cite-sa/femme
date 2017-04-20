package gr.cite.femme.engine.datastore.mongodb.codecs;

import gr.cite.femme.engine.datastore.mongodb.utils.FieldNames;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.model.SystemicMetadata;
import gr.cite.femme.core.model.Collection;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;

public class CollectionCodec extends ElementCodec<Collection> {

	public CollectionCodec(CodecRegistry codecRegistry) {
		super(codecRegistry);
	}

	@Override
	public Collection decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, name = null, endpoint = null;
		List<Metadatum> metadata = null;
		SystemicMetadata systemicMetadata =  null;

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
			}
		}

		reader.readEndDocument();

		return Collection.builder().id(id).name(name).endpoint(endpoint).metadata(metadata).systemicMetadata(systemicMetadata).build();
	}

	@Override
	public Class<Collection> getEncoderClass() {
		return Collection.class;
	}
}
