package gr.cite.commons.pipelinenew;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import gr.cite.commons.pipeline.exceptions.ProcessingPipelineException;
import gr.cite.commons.pipeline.exceptions.ProcessingPipelineHandlerException;
import gr.cite.commons.pipelinenew.handlers.ProcessingPipelineHandlerFactory;
import gr.cite.commons.pipeline.operations.ProcessingPipelineOperation;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.List;

public class PipelineManager {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private String pipelineConfig;
	
	public PipelineManager() throws IOException {
		this.pipelineConfig = Resources.toString(Resources.getResource("pipeline-config_new.json"), Charsets.UTF_8);
	}
	
	public PipelineManager(String pipelineConfigPath) throws IOException {
		this.pipelineConfig = Resources.toString(Resources.getResource(pipelineConfigPath), Charsets.UTF_8);
	}
	
	public void process(Object input) throws ProcessingPipelineException {
		//ProcessingPipelineCtx ctx = new ProcessingPipelineCtx(Collections.singletonList(input));
		List<ProcessingPipelineOperation> operations = null;
		try {
			operations = mapper.readValue(pipelineConfig, new TypeReference<List<ProcessingPipelineOperation>>(){});
		} catch (IOException e) {
			throw new ProcessingPipelineException("Error on deserializing pipeline configuration", e);
		}
		
		try {
			for (ProcessingPipelineOperation operation: operations) {
				if (input != null) {
					input = ProcessingPipelineHandlerFactory.getHandler(operation).process(input);
					System.out.println(input.getClass().getName());
				}
				System.out.println(input);
				
			}
		} catch (OperationNotSupportedException | ProcessingPipelineHandlerException e) {
			throw new ProcessingPipelineException(e);
		}
		
	}
}
