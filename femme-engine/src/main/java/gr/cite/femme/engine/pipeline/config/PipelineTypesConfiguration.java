package gr.cite.femme.engine.pipeline.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import gr.cite.commons.pipeline.config.PipelineConfiguration;
import gr.cite.commons.pipelinenew.Pipeline;
import gr.cite.femme.core.model.ElementType;
import gr.cite.pipelinenew.step.PipelineStep;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PipelineTypesConfiguration {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private Map<DatastoreType, Map<ElementType, List<PipelineStep>>> pipelines;
	
	public PipelineTypesConfiguration(Map<DatastoreType, Map<ElementType, List<PipelineStep>>> pipelines) {
		this.pipelines = pipelines;
	}
	
	public PipelineTypesConfiguration(String configuration) throws IOException {
		if (! Strings.isNullOrEmpty(configuration.trim())) {
			this.pipelines = mapper.readValue(configuration, new TypeReference<Map<DatastoreType, Map<ElementType, List<PipelineStep>>>>() {});
		}
	}
	
	public boolean isEmpty() {
		return this.pipelines == null || this.pipelines.isEmpty();
	}
	
	public Map<ElementType, List<PipelineStep>> getPipelineForDatastore(DatastoreType datastoreType) {
		return this.pipelines.get(datastoreType);
	}
	
	public Pipeline getPipelineForDatastoreTypeAndElementType(DatastoreType datastoreType, ElementType elementType) {
		return new Pipeline(this.pipelines.get(datastoreType).get(elementType));
	}
}
