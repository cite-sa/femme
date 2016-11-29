package gr.cite.femme.model;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

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

@JsonSerialize(using = CustomBBoxSerializer.class)
@JsonDeserialize(using = CustomBBoxDeserializer.class)
public class BBox {
	
	private String crs;
	
	private String geoJson;
	
	public BBox() {
		
	}
	
	public BBox(String crs, String geoJson) {
		this.crs = crs;
		this.geoJson = geoJson;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}
	
	
}

class CustomBBoxSerializer extends JsonSerializer<BBox> {

	@Override
	public void serialize(BBox value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		jgen.writeStartObject();
		jgen.writeStringField("crs", value.getCrs());
		jgen.writeFieldName("geoJson");
		jgen.writeRawValue(value.getGeoJson());
		jgen.writeEndObject();
		
	}
}

class CustomBBoxDeserializer extends JsonDeserializer<BBox> {

	@Override
	public BBox deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
		Map<String, Object> bbox = jsonParser.readValueAs(new TypeReference<Map<String, Object>>() {});
		
		String crs = (String) bbox.get("crs");
		String geoJson = (String) bbox.get("geoJson");
		
		return new BBox(crs,  geoJson);
	}
}