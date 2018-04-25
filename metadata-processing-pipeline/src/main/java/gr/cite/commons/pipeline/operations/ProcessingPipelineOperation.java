package gr.cite.commons.pipeline.operations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "operation", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = ExtractOperation.class, name = "EXTRACT"),
		@JsonSubTypes.Type(value = FilterOperation.class, name = "FILTER"),
		@JsonSubTypes.Type(value = TransformOperation.class, name = "TRANSFORM")
})

public abstract class ProcessingPipelineOperation {
	public enum OperationType {
		/*EXTRACT("extract"),
		ENRICH("enrich"),
		TRANSFORM("transform"),
		FILTER("filter");
		
		private String type;
		
		OperationType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return this.type;
		}*/
		EXTRACT,
		ENRICH,
		TRANSFORM,
		FILTER
	}
	
	public enum Datatype {
		INTEGER, DOUBLE, STRING, DATE
	}
	
	public enum Format {
		XML, JSON
	}
	
	private OperationType operation;
	
	public OperationType getOperation() {
		return operation;
	}
	
	public void setOperation(OperationType operation) {
		this.operation = operation;
	}
	
}
