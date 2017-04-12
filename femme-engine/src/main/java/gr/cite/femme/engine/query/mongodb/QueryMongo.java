package gr.cite.femme.engine.query.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gr.cite.femme.core.query.api.Query;
import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class QueryMongo implements Query<CriterionMongo> {
	
	@JsonProperty("criteria")
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
		List<Document> list = criteria.stream().map(CriterionMongo::build).collect(Collectors.toList());
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

	@Override
	public String toString() {
		return build().toJson();
	}
	
	public static QueryMongo fromString(String queryJson) throws JsonParseException, JsonMappingException, IOException {
		if (queryJson != null) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(queryJson, QueryMongo.class);
		}
		return null;
	}
}
