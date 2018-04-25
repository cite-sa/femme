package gr.cite.commons.pipeline.operations;

public class TransformOperation extends ProcessingPipelineOperation {
	private Datatype datatype;
	
	public Datatype getDatatype() {
		return datatype;
	}
	
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}
}
