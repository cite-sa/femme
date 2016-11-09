package gr.cite.femme.model;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(Include.NON_EMPTY)
public class SystemicMetadata {
	
	private String id;
	
	@JsonSerialize(using = CustomInstantSerializer.class)
	@JsonDeserialize(using = CustomInstantDeserializer.class)
	private Instant created;
	
	@JsonSerialize(using = CustomInstantSerializer.class)
	@JsonDeserialize(using = CustomInstantDeserializer.class)
	private Instant modified;
	
	/*private Map<String, MetadataStatistics> xPathFrequencies;*/
	

	public SystemicMetadata() {
		
	}
	
	public SystemicMetadata(String id, Instant created, Instant modified) {
		this.id = id;
		this.created = created;
		this.modified = modified;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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

	/*public Map<String, MetadataStatistics> getxPathFrequencies() {
		return xPathFrequencies;
	}

	public void setXPathFrequencies(Map<String, MetadataStatistics> xPathFrequencies) {
		this.xPathFrequencies = xPathFrequencies;
	}
	public void updateXPathFrequencies(String xPath) {
		if (xPath != null && !xPath.equals("")) {
			if (!xPathFrequencies.containsKey(xPath)) {
				xPathFrequencies.put(xPath, new MetadataStatistics());
			} else {
				xPathFrequencies.get(xPath).updateStatictics();
			}
		}
	}*/
}

class CustomInstantSerializer extends JsonSerializer<Instant> {

	@Override
	public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("timestamp", value.toEpochMilli());
		jgen.writeEndObject();
		
	}
}

class CustomInstantDeserializer extends JsonDeserializer<Instant> {

	@Override
	public Instant deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		Map<String, Long> instantMap = jsonParser.readValueAs(new TypeReference<Map<String, Long>>() {});
		
		Long epochMillis = instantMap.get("timestamp");
		
		Instant instant = Instant.ofEpochMilli((epochMillis));
		return instant;
	}
}