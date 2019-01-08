package gr.cite.pipelinenew.step;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.pipelinenew.step.filter.FilterStep;
import gr.cite.pipelinenew.step.map.MapStep;

import javax.naming.OperationNotSupportedException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "operation", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ExtractStep.class, name = "extract"),
	@JsonSubTypes.Type(value = FilterStep.class, name = "filter"),
	@JsonSubTypes.Type(value = MapStep.class, name = "map")
})

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class PipelineStep {
	private OperationType operation;
	
	public OperationType getOperation() {
		return operation;
	}
	
	public void setOperation(OperationType operation) {
		this.operation = operation;
	}
	
	public abstract Object process(Object input) throws OperationNotSupportedException;
}