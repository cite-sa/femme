package gr.cite.femme.client.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.CriterionInterface;

@JsonInclude(Include.NON_EMPTY)
public class CriterionClient implements CriterionInterface {

	@JsonProperty
	private OperatorClient root;
	
	@Override
	public OperatorClient root() {
		root = new OperatorClient(this);
		return root;
	}

}
