package gr.cite.femme.query.mongodb;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.cite.femme.query.api.CriterionInterface;

@JsonInclude(Include.NON_EMPTY)
public class CriterionMongo implements CriterionInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(CriterionMongo.class);
	
	@JsonProperty
	private OperatorMongo root;
	
	@Override
	public OperatorMongo root() {
		return root = root == null ? new OperatorMongo(this) : root;
	}
	
	public OperatorMongo getRoot() {
		return root;
	}
	
	public Map<String, Object> getQuery() {
		return null;
	}
	
	public Document build() {
		return root.build();
	}
	
}
