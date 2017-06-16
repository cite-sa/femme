package gr.cite.commons.pipeline.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = PipelineConfigurationDeserializer.class)
public class PipelineConfiguration {
	private static final ObjectMapper mapper = new ObjectMapper();

	private FilterOperation filter;
	private List<MapOperation> map;

	public FilterOperation getFilter() {
		return filter;
	}

	public void setFilter(FilterOperation filter) {
		this.filter = filter;
	}

	public List<MapOperation> getMap() {
		return map;
	}

	public void setMap(List<MapOperation> map) {
		this.map = map;
	}
}

class PipelineConfigurationDeserializer extends JsonDeserializer {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public Object deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
		PipelineConfiguration config = new PipelineConfiguration();
		JsonNode tree = parser.readValueAsTree();

		tree.fields().forEachRemaining(field -> {
			if ("filter".equals(field.getKey())) {
				if (field.getValue().isArray()) {
					FilterOperation filter = new FilterOperation();
					filter.setOperands(new ArrayList<>());
					filter.setSubOperations(new ArrayList<>());
					filter.setOperator("and");

					field.getValue().forEach(filterOperation -> {
						String conjuction = filterOperation.fieldNames().next();
						try {
							if ("and".equals(conjuction) || "or".equals(conjuction)) {
								filter.getSubOperations().add(mapper.readValue(filterOperation.toString(), FilterOperation.class));
							} else {
								filter.getOperands().add(mapper.readValue(filterOperation.toString(), FilterOperand.class));
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});

					config.setFilter(filter);
				}
			} else if ("map".equals(field.getKey())) {
				config.setMap(new ArrayList<>());
				field.getValue().forEach(mapOperation -> {
					try {
						config.getMap().add(mapper.readValue(mapOperation.toString(), MapOperation.class));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}
		});

		return config;
	}
}