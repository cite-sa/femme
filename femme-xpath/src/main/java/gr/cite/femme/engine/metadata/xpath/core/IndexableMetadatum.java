package gr.cite.femme.engine.metadata.xpath.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@JsonSerialize(using = CustomIndexableMetadatumSerializer.class)
@JsonDeserialize(using = CustomIndexableMetadatumDeserializer.class)
public class IndexableMetadatum {

    private String id;

    @JsonProperty("metadataSchemaId")
    private String metadataSchemaId;

    @JsonProperty("metadatumId")
    private String metadatumId;

    @JsonProperty("elementId")
    private String elementId;

    @JsonProperty("originalContentType")
    private String originalContentType;

    @JsonProperty("value")
    private String value;

    private Instant created;

    private Instant modified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMetadataSchemaId() {
        return metadataSchemaId;
    }

    public void setMetadataSchemaId(String metadataSchemaId) {
        this.metadataSchemaId = metadataSchemaId;
    }

    public String getMetadatumId() {
        return metadatumId;
    }

    public void setMetadatumId(String metadatumId) {
        this.metadatumId = metadatumId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getOriginalContentType() {
        return originalContentType;
    }

    public void setOriginalContentType(String originalContentType) {
        this.originalContentType = originalContentType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }
}

class CustomIndexableMetadatumSerializer extends JsonSerializer<IndexableMetadatum> {
    @Override
    public void serialize(IndexableMetadatum value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("metadataSchemaId", value.getMetadataSchemaId());
        jgen.writeStringField("metadatumId", value.getMetadatumId());
        jgen.writeStringField("elementId", value.getElementId());
        jgen.writeStringField("originalContentType", value.getOriginalContentType());
        jgen.writeFieldName("value");
        /*jgen.writeStartArray(1);*/
        jgen.writeRawValue(value.getValue());
        /*jgen.writeEndArray();*/
        jgen.writeEndObject();
    }
}

class CustomIndexableMetadatumDeserializer extends JsonDeserializer<IndexableMetadatum> {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public IndexableMetadatum deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        Map<String, Object> indexableMetadatumInMap = jsonParser.readValueAs(new TypeReference<Map<String, Object>>() {});

        String metadataSchemaId = (String) indexableMetadatumInMap.get("metadataSchemaId");
        String metadatumId = (String) indexableMetadatumInMap.get("metadatumId");
        String elementId = (String) indexableMetadatumInMap.get("elementId");
        String originalContentType = (String) indexableMetadatumInMap.get("originalContentType");
        String value = indexableMetadatumInMap.get("value") != null
                ? mapper.writeValueAsString(indexableMetadatumInMap.get("value"))
                : null;

        IndexableMetadatum indexableMetadatum = new IndexableMetadatum();
        indexableMetadatum.setMetadataSchemaId(metadataSchemaId);
        indexableMetadatum.setElementId(elementId);
        indexableMetadatum.setMetadatumId(metadatumId);
        indexableMetadatum.setOriginalContentType(originalContentType);
        indexableMetadatum.setValue(value);
        return indexableMetadatum;

    }
}
