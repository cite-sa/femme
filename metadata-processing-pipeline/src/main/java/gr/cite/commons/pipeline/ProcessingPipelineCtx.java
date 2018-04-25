package gr.cite.commons.pipeline;

import java.util.List;

public class ProcessingPipelineCtx {
	private List<String> input;
	//private List<Object> output;
	
	
	public ProcessingPipelineCtx(List<String> input) {
		this.input = input;
	}
	
	public List<String> getInput() {
		return input;
	}
	
	public void setInput(List<String> input) {
		this.input = input;
	}
}
