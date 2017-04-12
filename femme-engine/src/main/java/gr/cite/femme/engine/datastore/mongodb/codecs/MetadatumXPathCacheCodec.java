package gr.cite.femme.engine.datastore.mongodb.codecs;

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
import org.bson.types.ObjectId;

import gr.cite.femme.core.model.MetadatumXPathCache;

public class MetadatumXPathCacheCodec implements CollectibleCodec<MetadatumXPathCache> {
	
	private static final String METADATUM_INDEX_ID_KEY = "_id";
	
	private static final String METADATUM_INDEX_XPATH_KEY = "xPath";
	
	private static final String METADATUM_INDEX_VALUES_KEY = "values";

	@Override
	public void encode(BsonWriter writer, MetadatumXPathCache value, EncoderContext encoderContext) {
		writer.writeStartDocument();

		if (!documentHasId(value)) {
			generateIdIfAbsentFromDocument(value);
		}
		
		if (value.getId() != null) {
			writer.writeObjectId(METADATUM_INDEX_ID_KEY, new ObjectId(value.getId()));
		}
		
		if (value.getXPath() != null) {
			writer.writeString(METADATUM_INDEX_XPATH_KEY, value.getXPath());
		}
		
		if (value.getValues() != null) {
			writer.writeName(METADATUM_INDEX_VALUES_KEY);
			writer.writeStartArray();
			for (String xPathValue: value.getValues()) {
				writer.writeString(xPathValue);
			}
			writer.writeEndArray();
		}
		
		writer.writeEndDocument();
	}

	@Override
	public Class<MetadatumXPathCache> getEncoderClass() {
		return MetadatumXPathCache.class;
	}

	@Override
	public MetadatumXPathCache decode(BsonReader reader, DecoderContext decoderContext) {
		String id = null, xPath = null;
		List<String> xPathValues = new ArrayList<>();
		
		reader.readStartDocument();
		
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            
            if (fieldName.equals(METADATUM_INDEX_ID_KEY)) {
            	id = reader.readObjectId().toString();
            } else if (fieldName.equals(METADATUM_INDEX_XPATH_KEY)) {
            	xPath = reader.readString();
            } else if (fieldName.equals(METADATUM_INDEX_VALUES_KEY)) {
            	reader.readStartArray();
            	
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                	xPathValues.add(reader.readString());
                }
                
        		reader.readEndArray();
            }
		}
		
		reader.readEndDocument();

		MetadatumXPathCache metadatumIndex = new MetadatumXPathCache();
		metadatumIndex.setId(id);
		metadatumIndex.setXPath(xPath);
		metadatumIndex.setValues(xPathValues);

		return metadatumIndex;
	}

	@Override
	public MetadatumXPathCache generateIdIfAbsentFromDocument(MetadatumXPathCache metadatumIndex) {
		if (!documentHasId(metadatumIndex)) {
			metadatumIndex.setId(new ObjectId().toString());
		}
		return metadatumIndex;
	}

	@Override
	public boolean documentHasId(MetadatumXPathCache metadatumIndex) {
		return metadatumIndex.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(MetadatumXPathCache metadatumIndex) {
		if (!documentHasId(metadatumIndex)) {
			throw new IllegalStateException("The metadatum insert does not contain an _id");
		}
		return new BsonString(metadatumIndex.getId());
	}

}
