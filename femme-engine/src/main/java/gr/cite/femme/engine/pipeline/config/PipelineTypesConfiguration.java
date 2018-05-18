package gr.cite.femme.engine.pipeline.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import gr.cite.commons.pipeline.config.PipelineConfiguration;
import gr.cite.femme.core.model.ElementType;

import java.io.IOException;
import java.util.Map;

public class PipelineTypesConfiguration {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private Map<DatastoreType, Map<ElementType, PipelineConfiguration>> configuration;
	
	public PipelineTypesConfiguration(Map<DatastoreType, Map<ElementType, PipelineConfiguration>> configuration) {
		this.configuration = configuration;
	}
	
	public PipelineTypesConfiguration(String configuration) throws IOException {
		if (! Strings.isNullOrEmpty(configuration.trim())) {
			this.configuration = mapper.readValue(configuration, new TypeReference<Map<DatastoreType, Map<ElementType, PipelineConfiguration>>>() {});
		}
	}
	
	public boolean isEmpty() {
		return this.configuration == null || this.configuration.isEmpty();
	}
	
	public Map<ElementType, PipelineConfiguration> getConfigurationForDatastore(DatastoreType datastoreType) {
		return this.configuration.get(datastoreType);
	}
	
	public PipelineConfiguration getConfigurationForDatastoreTypeAndElementType(DatastoreType datastoreType, ElementType elementType) {
		return this.configuration.get(datastoreType).get(elementType);
	}
}
