package gr.cite.femme.client.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.cite.femme.query.api.Query;

@JsonInclude(Include.NON_EMPTY)
public class QueryClient implements Query<CriterionClient> {

	@JsonProperty
	private List<CriterionClient> criteria;
	
	public QueryClient() {
		criteria = new ArrayList<>();
	}
	
	@Override
	public Query<CriterionClient> addCriterion(CriterionClient criterion) {
		criteria.add(criterion);
		return this;
	}

	@Override
	public List<CriterionClient> getCriteria() {
		return criteria;
	}

}
