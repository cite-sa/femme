package gr.cite.commons.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import gr.cite.commons.pipeline.config.FilterOperand;
import gr.cite.commons.pipeline.config.FilterOperation;
import gr.cite.commons.pipeline.config.MapOperation;
import gr.cite.commons.pipeline.config.PipelineConfiguration;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessingPipeline {
	private static final ObjectMapper mapper = new ObjectMapper();

	private PipelineConfiguration config;
	private Map<String, Object> output;

	public ProcessingPipeline() throws IOException {
		this(mapper.readValue(Resources.toString(Resources.getResource("pipeline-config.json"), Charsets.UTF_8), PipelineConfiguration.class));
	}

	public ProcessingPipeline(PipelineConfiguration config) {
		this.config = config;
		this.output = new HashMap<>();
	}

	public Map<String, Object> process(String input, String format) throws ProcessingPipelineException {
		Map<String, Object> output = new HashMap<>();

		if (applyFilter(input, format, this.config.getFilter())) {
			for (MapOperation mapOperation: this.config.getMap()) {
				try {
					applyMap(input, format, mapOperation);
				} catch (OperationNotSupportedException e) {
					throw new ProcessingPipelineException(e);
				}
			}
		}

		return this.output;
	}

	private boolean applyFilter(String input, String format, FilterOperation filter) {
		if (filter != null) {
			for (FilterOperand operand: filter.getOperands()) {
				if (format.equals(operand.getFormat())) {
					try {
						List<String> queryResult = FilterExecution.applyQuery(operand.getQuery(), input, operand.getFormat());
						if (!FilterExecution.applyOperator(queryResult, operand.getOperator(), operand.getValue(), operand.getType())) {
							return false;
						}
					} catch (OperationNotSupportedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return true;
	}

	private void applyMap(String input, String format, MapOperation map) throws OperationNotSupportedException {
		if (map != null) {
			if (format.equals(map.getFormat())) {
				List<String> queryResult = MapExecution.applyQuery(map.getQuery(), input, map.getFormat());
				if (map.isArray()) {
					this.output.put(map.getName(), queryResult);
				} else {
					this.output.put(map.getName(), queryResult.get(0));
				}
			}
		}
	}


}
