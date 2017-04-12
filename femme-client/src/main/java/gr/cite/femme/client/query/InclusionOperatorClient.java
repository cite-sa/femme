package gr.cite.femme.client.query;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.cite.femme.core.query.api.InclusionOperator;

@JsonInclude(Include.NON_EMPTY)
public class InclusionOperatorClient implements InclusionOperator<CriterionClient> {

	private static enum InclusionOperators {
		IN_COLLECTIONS("in_collections"),
		IN_ANY_COLLECTION("in_any_collection"),
		HAS_DATA_ELEMENTS("has_data_elements"),
		HAS_ANY_DATA_ELEMENT("has_any_data_element");
		
		private String inclusionOperatorCode;
		
		InclusionOperators(String inclusionOperatorCode) {
			this.inclusionOperatorCode = inclusionOperatorCode;
		}
		
		public String getInclusionOperatorCode() {
			return inclusionOperatorCode;
		}
	}
		
	@JsonProperty
	private InclusionOperators operator;
	
	@JsonProperty
	private List<CriterionClient> criteria;
	
	@Override
	public void inCollections(List<CriterionClient> criteria) {
		operator = InclusionOperators.IN_COLLECTIONS;
		this.criteria = criteria;
		
	}

	@Override
	public void inAnyCollection(List<CriterionClient> criteria) {
		operator = InclusionOperators.IN_ANY_COLLECTION;
		this.criteria = criteria;
		
	}

	@Override
	public void hasDataElements(List<CriterionClient> criteria) {
		operator = InclusionOperators.HAS_DATA_ELEMENTS;
		this.criteria = criteria;
		
	}

	@Override
	public void hasAnyDataElement(List<CriterionClient> criteria) {
		operator = InclusionOperators.HAS_ANY_DATA_ELEMENT;
		this.criteria = criteria;
		
	}
	
}
