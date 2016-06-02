package gr.cite.femme.query.serialization.mongodb;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Query;

@JsonSerialize(using = CustomQuerySerializer.class)
public class QuerySerializer {
	private static final Logger logger = LoggerFactory.getLogger(QuerySerializer.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	@JsonProperty
	private String operator;

	@JsonProperty
	private List<CriteriaSerializer> criteria;

	
	public QuerySerializer() {

	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<CriteriaSerializer> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<CriteriaSerializer> criteria) {
		this.criteria = criteria;
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "Error in serialization";
		}
	}

	public static QuerySerializer valueOf(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, QuerySerializer.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuerySerializer other = (QuerySerializer) obj;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		return true;
	}
	
	public Query build() {
		List<Criteria> criteriaList = criteria.stream().map(new Function<CriteriaSerializer, Criteria>() {

			@Override
			public Criteria apply(CriteriaSerializer crit) {
				return crit.build();
			}
		}).collect(Collectors.toList());
		
		Criteria finalCriteria;
		if (operator != null) {
			finalCriteria = new Criteria();
			switch (operator) {
			case Operation.OR_OPERATOR:
				finalCriteria.orOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
				break;

			default:
				break;
			}
		} else {
			finalCriteria = criteriaList.get(0);
		}
		
		Query query = new Query();
		query.addCriteria(finalCriteria);
		return query;
	}
	
	

}

class CustomQuerySerializer extends JsonSerializer<QuerySerializer> {

	@Override
	public void serialize(QuerySerializer value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		jgen.writeObject(value);
	}
}