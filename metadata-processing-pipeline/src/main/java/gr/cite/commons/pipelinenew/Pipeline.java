package gr.cite.commons.pipelinenew;

import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.pipelinenew.step.PipelineStep;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Pipeline {

	private List<PipelineStep> steps;
	
	public Pipeline(PipelineStep... steps) {
		this.steps = Arrays.asList(steps);
	}
	
	public Pipeline(List<PipelineStep> steps) {
		this.steps = steps;
	}
	
	public List<PipelineStep> getSteps() {
		return steps;
	}
	
	public void setSteps(List<PipelineStep> steps) {
		this.steps = steps;
	}
	
	public Map<String, Object> process(Object input) throws OperationNotSupportedException {
		Object processed = input;
		for (PipelineStep step: steps) {
			processed = step.process(processed);
			if (processed == null) return null;
		}
		return (Map<String, Object>) processed;
	}
}