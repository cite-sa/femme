package gr.cite.femme.query.mongodb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.cite.femme.query.api.Query;

@JsonInclude(Include.NON_EMPTY)
public class QueryMongo implements Query<CriterionMongo> {
	
	@JsonProperty
	private List<CriterionMongo> criteria;

	private QueryMongo() {
		criteria = new ArrayList<>();
	}
	
	public static QueryMongo query() {
		return new QueryMongo();
	}
	
	@Override
	public QueryMongo addCriterion(CriterionMongo criterion) {
		criteria.add(criterion);
		return this;
	}

	@Override
	public List<CriterionMongo> getCriteria() {
		return criteria;
	}
	
	public Document build() {
		List<Document> list = new ArrayList<>();
		criteria.stream().map(criterion -> criterion.build()).forEach(list::add);
		return list.size() == 1 ? list.get(0) : new Document().append("$and", list); 
		
		/*String query = "{";
		Iterator<CriterionMongo> criteriaIterator = criteria.iterator();
		while (criteriaIterator.hasNext()) {
			query += criteriaIterator.next().build();
			if (criteriaIterator.hasNext()) {
				query += ",";
			}
		}
		query += "}";
		return query;*/
	}
}
