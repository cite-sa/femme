package gr.cite.femme.query.serialization.mongodb;

import java.io.IOException;
import java.util.List;

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

import gr.cite.femme.exceptions.InvalidCriteriaQueryOperation;
import gr.cite.femme.query.mongodb.Criteria;
import gr.cite.femme.query.mongodb.Where;

@JsonSerialize(using = CustomCriteriaSerializer.class)
public class CriteriaSerializer {

	private static final Logger logger = LoggerFactory.getLogger(CriteriaSerializer.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	@JsonProperty
	private String operator;
	
	@JsonProperty
	private List<OperatorSerializer> criteria;


	public CriteriaSerializer() {

	}
	
	

	public String getOperator() {
		return operator;
	}



	public void setOperator(String operator) {
		this.operator = operator;
	}



	public List<OperatorSerializer> getCriteria() {
		return criteria;
	}



	public void setCriteria(List<OperatorSerializer> criteria) {
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

	public static CriteriaSerializer valueOf(String json) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, CriteriaSerializer.class);
	}

	public Criteria build() {
		Criteria criteriaObj = new Criteria();

		Where where = null;
		for (OperatorSerializer operator : criteria) {
			try {
				switch (operator.getOperator()) {
				case Operation.WHERE:
					where = criteriaObj.where(operator.getFieldName());
					break;
				case Operation.AND:
					where = criteriaObj.and(operator.getFieldName());
					break;
				default:
					break;
				}
			
				switch (operator.getOperation()) {
					case Operation.EQ:
						where.eq(operator.getValue());
						break;
	
					default:
						break;
				}
			} catch (InvalidCriteriaQueryOperation e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		return criteriaObj;
	}

}

class CustomCriteriaSerializer extends JsonSerializer<CriteriaSerializer> {

	@Override
	public void serialize(CriteriaSerializer value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		jgen.writeObject(value);
	}
}
