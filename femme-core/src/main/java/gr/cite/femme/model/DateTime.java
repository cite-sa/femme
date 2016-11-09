package gr.cite.femme.model;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = CustomDateTimeSerializer.class)
@JsonDeserialize(using = CustomDateTimeDeserializer.class)
public class DateTime {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private ZonedDateTime zonedDateTime;
	
	public DateTime() {
		zonedDateTime = ZonedDateTime.now();
	}
	
	public DateTime(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public void setZonedDateTime(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}
	
	public static CustomDateTimeSerializer valueOf(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, CustomDateTimeSerializer.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((zonedDateTime == null) ? 0 : zonedDateTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateTime other = (DateTime) obj;
		if (zonedDateTime == null) {
			if (other.zonedDateTime != null)
				return false;
		} else if (!zonedDateTime.equals(other.zonedDateTime))
			return false;
		return true;
	}
	
	public static void main(String[] args) {
		LocalDateTime local = LocalDateTime.now();
		ZonedDateTime zoned = ZonedDateTime.now();
		ZonedDateTime zonedParsed = ZonedDateTime.ofInstant(Instant.ofEpochMilli(zoned.toInstant().toEpochMilli()), ZoneId.of(zoned.getZone().getId()));
		Instant instant = Instant.now();
		
		System.out.println(zoned.getOffset());
		System.out.println(zoned.getZone());
		System.out.println(zoned.getZone().getId());
		System.out.println(ZoneId.getAvailableZoneIds());
		
		System.out.println(local);
		System.out.println(zoned);
		System.out.println(zonedParsed);
		System.out.println(instant);
	}
	
}

class CustomDateTimeSerializer extends JsonSerializer<DateTime> {

	@Override
	public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("timestamp", value.getZonedDateTime().toInstant().toEpochMilli());
		jgen.writeStringField("offset", value.getZonedDateTime().getOffset().getId());
		jgen.writeStringField("zone", value.getZonedDateTime().getZone().getId());
		jgen.writeEndObject();
		
	}
}

class CustomDateTimeDeserializer extends JsonDeserializer<DateTime> {

	@Override
	public DateTime deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		Map<String, Object> dateTimeMap = jsonParser.readValueAs(new TypeReference<Map<String, Object>>() {});
		
		long epochMillis = (long) dateTimeMap.get("timestamp");
		
		Instant instant = Instant.ofEpochMilli((epochMillis));
		ZoneId zoneId = ZoneId.of((String)dateTimeMap.get("zone"));
		DateTime dateTime = new DateTime(ZonedDateTime.ofInstant(instant, zoneId));
		return dateTime;
	}
}
