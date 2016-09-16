package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gr.cite.femme.query.api.InclusionOperator;
import gr.cite.femme.utils.Pair;

@JsonInclude(Include.NON_EMPTY)
public class InclusionOperatorMongo implements InclusionOperator<CriterionMongo> {
	
	protected static enum InclusionOperator {
		IN_COLLECTIONS("$in_collections"),
		IN_ANY_COLLECTION("$in_any_collection"),
		HAS_DATA_ELEMENTS("$has_data_elements"),
		HAS_ANY_DATA_ELEMENT("$has_any_data_element");
		
		private String inclusionOperatorCode;
		
		private InclusionOperator(String inclusionOperatorCode) {
			this.inclusionOperatorCode = inclusionOperatorCode;
		}
		
		protected String getLogicalOperatorCode() {
			return inclusionOperatorCode;
		}
	}
	
	@JsonProperty
	private InclusionOperator operator;
	
	@JsonProperty
	private List<CriterionMongo> criteria;
	
	@Override
	public void inCollections(List<CriterionMongo> criteria) {
		operator = InclusionOperator.IN_COLLECTIONS;
		this.criteria = criteria;
	}
	
	@Override
	public void inAnyCollection(List<CriterionMongo> criteria) {
		operator = InclusionOperator.IN_ANY_COLLECTION;
		this.criteria = criteria;
	}

	@Override
	public void hasDataElements(List<CriterionMongo> criteria) {
		operator = InclusionOperator.HAS_DATA_ELEMENTS;
		this.criteria = criteria;
	}

	@Override
	public void hasAnyDataElement(List<CriterionMongo> criteria) {
		operator = InclusionOperator.HAS_ANY_DATA_ELEMENT;
		this.criteria = criteria;
	}
	
	protected Pair<String, List<Document>> build() {
		
		List<Document> criteriaDocuments = new ArrayList<>();
		for (CriterionMongo criterion : criteria) {
			criteriaDocuments.add(criterion.build());
		}
		
		Pair<String, List<Document>> pair = new Pair<>(operator.getLogicalOperatorCode(), criteriaDocuments);
		return pair;
		
	}

}
